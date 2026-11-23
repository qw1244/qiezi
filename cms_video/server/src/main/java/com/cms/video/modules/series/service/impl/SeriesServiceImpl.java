package com.cms.video.modules.series.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.interact.entity.Fav;
import com.cms.video.modules.interact.entity.VLike;
import com.cms.video.modules.interact.entity.WatchLog;
import com.cms.video.modules.interact.mapper.FavMapper;
import com.cms.video.modules.interact.mapper.VLikeMapper;
import com.cms.video.modules.interact.mapper.WatchLogMapper;
import com.cms.video.modules.series.dto.SeriesSaveReq;
import com.cms.video.modules.series.entity.Series;
import com.cms.video.modules.series.mapper.SeriesMapper;
import com.cms.video.modules.series.service.SeriesService;
import com.cms.video.modules.series.vo.SeriesDetailVo;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesMapper seriesMapper;
    private final VideoMapper videoMapper;
    private final FavMapper favMapper;
    private final VLikeMapper vLikeMapper;
    private final WatchLogMapper watchLogMapper;
    private final CosService cosService;

    @Override
    public PageVo<SeriesVo> pageFront(String keyword, String category, String tag, String sort, long page, long size, Long userId) {
        // 用 QueryWrapper：点赞排序要按子查询 (SELECT SUM(like_count) FROM video ...) 排，
        // LambdaQueryWrapper 写不了这种表达式排序。
        QueryWrapper<Series> qw = new QueryWrapper<Series>().eq("status", 1);
        if (StringUtils.hasText(keyword)) {
            qw.like("title", keyword);
        }
        if (StringUtils.hasText(category)) {
            qw.eq("category", category);
        }
        if (StringUtils.hasText(tag)) {
            // tags 是逗号分隔，FIND_IN_SET 精确匹配单个标签，避免 LIKE 把“重生”匹配到“重生之我在”
            qw.apply("FIND_IN_SET({0}, tags)", tag.trim());
        }
        if ("hot".equals(sort)) {
            // 当前热播：合集播放数倒序（VideoServiceImpl 每次播放都会 +1）
            qw.orderByDesc("play_count");
        } else if ("likes".equals(sort)) {
            // 点赞最多：合集下所有已上架单集的点赞数之和倒序
            qw.orderByDesc("(SELECT COALESCE(SUM(like_count),0) FROM video WHERE video.series_id = series.id AND video.status = 1)");
        }
        // 最新更新默认按 id 倒序（不用 updated_at：播放计数更新也会刷新 updated_at，会把热播的顶上来）
        qw.orderByDesc("id");
        Page<Series> p = seriesMapper.selectPage(new Page<>(page, size), qw);
        List<SeriesVo> list = p.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        fillUserFlags(list, userId);
        fillLikeCounts(list);
        return PageVo.of(p.getTotal(), list);
    }

    /** 一次分组查询补齐当前页每个合集的点赞总数（合集下各单集 like_count 之和）。 */
    private void fillLikeCounts(List<SeriesVo> list) {
        if (list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(SeriesVo::getId).collect(Collectors.toList());
        List<Map<String, Object>> rows = videoMapper.selectMaps(new QueryWrapper<Video>()
                .select("series_id", "COALESCE(SUM(like_count),0) AS total")
                .eq("status", 1)
                .in("series_id", ids)
                .groupBy("series_id"));
        Map<Long, Long> totals = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object sid = row.get("series_id");
            Object total = row.get("total");
            if (sid instanceof Number && total instanceof Number) {
                totals.put(((Number) sid).longValue(), ((Number) total).longValue());
            }
        }
        list.forEach(s -> s.setLikeCount(totals.getOrDefault(s.getId(), 0L)));
    }

    @Override
    public SeriesDetailVo detail(Long id, Long userId) {
        Series series = seriesMapper.selectById(id);
        if (series == null || (series.getStatus() != null && series.getStatus() == 0)) {
            throw new BizException("合集不存在");
        }
        SeriesDetailVo vo = toDetail(series);
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getSeriesId, id)
                .eq(Video::getStatus, 1)
                .orderByAsc(Video::getEpNo));
        Set<Long> liked = Collections.emptySet();
        Map<Long, Integer> progressMap = new HashMap<>();
        if (userId != null) {
            Fav fav = favMapper.selectOne(new LambdaQueryWrapper<Fav>()
                    .eq(Fav::getUserId, userId).eq(Fav::getSeriesId, id));
            vo.setFav(fav != null);
            List<Long> ids = videos.stream().map(Video::getId).collect(Collectors.toList());
            if (!ids.isEmpty()) {
                liked = vLikeMapper.selectList(new LambdaQueryWrapper<VLike>()
                                .eq(VLike::getUserId, userId).in(VLike::getVideoId, ids))
                        .stream().map(VLike::getVideoId).collect(Collectors.toSet());
                watchLogMapper.selectList(new LambdaQueryWrapper<WatchLog>()
                                .eq(WatchLog::getUserId, userId).in(WatchLog::getVideoId, ids))
                        .forEach(w -> progressMap.put(w.getVideoId(), w.getProgress()));
            }
            WatchLog latest = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                    .eq(WatchLog::getUserId, userId)
                    .eq(WatchLog::getSeriesId, id)
                    .orderByDesc(WatchLog::getUpdatedAt)
                    .last("limit 1"));
            if (latest != null) {
                vo.setResumeVideoId(latest.getVideoId());
                vo.setResumeProgress(latest.getProgress());
            }
        } else {
            vo.setFav(false);
        }
        Set<Long> finalLiked = liked;
        vo.setVideos(videos.stream().map(v -> {
            VideoVo vv = toVideoVo(v);
            vv.setLiked(finalLiked.contains(v.getId()));
            vv.setProgress(progressMap.getOrDefault(v.getId(), 0));
            return vv;
        }).collect(Collectors.toList()));
        fillFirstProgress(Collections.singletonList(vo), userId);
        return vo;
    }

    @Override
    public PageVo<SeriesVo> pageAdmin(String keyword, Integer status, long page, long size) {
        LambdaQueryWrapper<Series> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Series::getTitle, keyword);
        }
        if (status != null) {
            qw.eq(Series::getStatus, status);
        }
        qw.orderByDesc(Series::getId);
        Page<Series> p = seriesMapper.selectPage(new Page<>(page, size), qw);
        return PageVo.of(p.getTotal(), p.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
    }

    @Override
    public SeriesDetailVo adminDetail(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BizException("合集不存在");
        }
        SeriesDetailVo vo = toDetail(series);
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getSeriesId, id)
                .orderByAsc(Video::getEpNo));
        vo.setVideos(videos.stream().map(this::toVideoVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public Long save(SeriesSaveReq req) {
        Series series;
        if (req.getId() == null) {
            series = new Series();
            series.setPlayCount(0L);
            series.setFavCount(0L);
            series.setEpCount(0);
        } else {
            series = seriesMapper.selectById(req.getId());
            if (series == null) {
                throw new BizException("合集不存在");
            }
        }
        series.setTitle(req.getTitle().trim());
        series.setCover(req.getCover() == null ? "" : req.getCover());
        series.setIntro(req.getIntro() == null ? "" : req.getIntro());
        series.setCategory(StringUtils.hasText(req.getCategory()) ? req.getCategory().trim() : "AI");
        series.setTags(normalizeTags(req.getTags()));
        series.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        if (req.getId() == null) {
            seriesMapper.insert(series);
        } else {
            seriesMapper.updateById(series);
        }
        return series.getId();
    }

    @Override
    public Long createByUser(Long userId, SeriesSaveReq req) {
        Series series = new Series();
        series.setUserId(userId);
        series.setTitle(req.getTitle().trim());
        series.setCover("");
        series.setIntro(req.getIntro() == null ? "" : req.getIntro());
        series.setCategory(StringUtils.hasText(req.getCategory()) ? req.getCategory().trim() : "AI");
        series.setTags("");
        series.setStatus(0);
        series.setPlayCount(0L);
        series.setFavCount(0L);
        series.setEpCount(0);
        seriesMapper.insert(series);
        return series.getId();
    }

    @Override
    public PageVo<SeriesVo> mySeries(Long userId, long page, long size) {
        Page<Series> p = seriesMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Series>().eq(Series::getUserId, userId).orderByDesc(Series::getId));
        return PageVo.of(p.getTotal(), p.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
    }

    @Override
    public PageVo<SeriesVo> pendingSeries(long page, long size) {
        Page<Series> p = seriesMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Series>().eq(Series::getStatus, 0).orderByDesc(Series::getId));
        return PageVo.of(p.getTotal(), p.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void approveSeries(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BizException("合集不存在");
        }
        if (series.getStatus() == null || series.getStatus() != 0) {
            throw new BizException("该合集不是待审核状态");
        }
        series.setStatus(1);
        seriesMapper.updateById(series);
        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getSeriesId, id)
                .eq(Video::getStatus, 0)
                .set(Video::getStatus, 1));
    }

    @Override
    @Transactional
    public void rejectSeries(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BizException("合集不存在");
        }
        if (series.getStatus() == null || series.getStatus() != 0) {
            throw new BizException("该合集不是待审核状态");
        }
        remove(id);
    }

    @Override
    public void setCover(Long userId, Long id, String cover) {
        Series series = seriesMapper.selectById(id);
        if (series == null || series.getUserId() == null || !series.getUserId().equals(userId)) {
            throw new BizException("合集不存在");
        }
        series.setCover(cover == null ? "" : cover);
        seriesMapper.updateById(series);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>().eq(Video::getSeriesId, id));
        for (Video video : videos) {
            if (StringUtils.hasText(video.getCosKey())) {
                try {
                    cosService.deleteObject(video.getCosKey());
                } catch (Exception ignored) {
                }
            }
            videoMapper.deleteById(video.getId());
        }
        seriesMapper.deleteById(id);
        favMapper.delete(new LambdaQueryWrapper<Fav>().eq(Fav::getSeriesId, id));
    }

    private void fillUserFlags(List<SeriesVo> list, Long userId) {
        if (userId == null || list.isEmpty()) {
            list.forEach(s -> s.setFav(false));
            return;
        }
        List<Long> ids = list.stream().map(SeriesVo::getId).collect(Collectors.toList());
        Set<Long> favIds = favMapper.selectList(new LambdaQueryWrapper<Fav>()
                        .eq(Fav::getUserId, userId).in(Fav::getSeriesId, ids))
                .stream().map(Fav::getSeriesId).collect(Collectors.toSet());
        list.forEach(s -> s.setFav(favIds.contains(s.getId())));
        fillFirstProgress(list, userId);
    }

    private void fillFirstProgress(List<? extends SeriesVo> list, Long userId) {
        if (userId == null || list.isEmpty()) {
            list.forEach(s -> s.setFirstProgress(0));
            return;
        }
        for (SeriesVo s : list) {
            Video first = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                    .eq(Video::getSeriesId, s.getId())
                    .eq(Video::getEpNo, 1)
                    .last("limit 1"));
            if (first == null || first.getDuration() == null || first.getDuration() <= 0) {
                s.setFirstProgress(0);
                continue;
            }
            WatchLog log = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                    .eq(WatchLog::getUserId, userId)
                    .eq(WatchLog::getVideoId, first.getId())
                    .last("limit 1"));
            if (log == null || log.getProgress() == null) {
                s.setFirstProgress(0);
            } else {
                int pct = (int) Math.min(100, Math.round(log.getProgress() * 100.0 / first.getDuration()));
                s.setFirstProgress(pct);
            }
        }
    }

    private SeriesVo toVo(Series s) {
        SeriesVo vo = new SeriesVo();
        vo.setId(s.getId());
        vo.setTitle(s.getTitle());
        vo.setCover(s.getCover());
        vo.setIntro(s.getIntro());
        vo.setCategory(s.getCategory());
        vo.setTags(s.getTags());
        vo.setStatus(s.getStatus());
        vo.setEpCount(s.getEpCount());
        vo.setPlayCount(s.getPlayCount());
        vo.setFavCount(s.getFavCount());
        return vo;
    }

    private SeriesDetailVo toDetail(Series s) {
        SeriesDetailVo vo = new SeriesDetailVo();
        vo.setId(s.getId());
        vo.setTitle(s.getTitle());
        vo.setCover(s.getCover());
        vo.setIntro(s.getIntro());
        vo.setCategory(s.getCategory());
        vo.setTags(s.getTags());
        vo.setStatus(s.getStatus());
        vo.setEpCount(s.getEpCount());
        vo.setPlayCount(s.getPlayCount());
        vo.setFavCount(s.getFavCount());
        return vo;
    }

    /** 标签归一化：支持中英文逗号、顿号分隔，去空格、去重，用英文逗号拼回。空返回 ""。 */
    private String normalizeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return "";
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String t : tags.split("[,，、]")) {
            String v = t.trim();
            if (!v.isEmpty()) {
                set.add(v);
            }
        }
        return String.join(",", set);
    }

    private VideoVo toVideoVo(Video v) {
        VideoVo vo = new VideoVo();
        vo.setId(v.getId());
        vo.setSeriesId(v.getSeriesId());
        vo.setTitle(v.getTitle());
        vo.setEpNo(v.getEpNo());
        vo.setCosKey(v.getCosKey());
        vo.setCover(v.getCover());
        vo.setDuration(v.getDuration());
        vo.setLikeCount(v.getLikeCount());
        vo.setPlayCount(v.getPlayCount());
        vo.setStatus(v.getStatus());
        vo.setPlayUrl(cosService.buildPlayUrl(v.getCosKey()));
        vo.setCreatedAt(v.getCreatedAt());
        return vo;
    }
}
