package com.cms.video.modules.series.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public PageVo<SeriesVo> pageFront(String keyword, long page, long size, Long userId) {
        LambdaQueryWrapper<Series> qw = new LambdaQueryWrapper<Series>().eq(Series::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            qw.like(Series::getTitle, keyword);
        }
        qw.orderByDesc(Series::getId);
        Page<Series> p = seriesMapper.selectPage(new Page<>(page, size), qw);
        List<SeriesVo> list = p.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        fillUserFlags(list, userId);
        return PageVo.of(p.getTotal(), list);
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
        series.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        if (req.getId() == null) {
            seriesMapper.insert(series);
        } else {
            seriesMapper.updateById(series);
        }
        return series.getId();
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
        vo.setStatus(s.getStatus());
        vo.setEpCount(s.getEpCount());
        vo.setPlayCount(s.getPlayCount());
        vo.setFavCount(s.getFavCount());
        return vo;
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
        return vo;
    }
}
