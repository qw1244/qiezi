package com.cms.video.modules.interact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.interact.dto.WatchReq;
import com.cms.video.modules.interact.entity.Fav;
import com.cms.video.modules.interact.entity.VLike;
import com.cms.video.modules.interact.entity.WatchLog;
import com.cms.video.modules.interact.mapper.FavMapper;
import com.cms.video.modules.interact.mapper.VLikeMapper;
import com.cms.video.modules.interact.mapper.WatchLogMapper;
import com.cms.video.modules.interact.service.InteractService;
import com.cms.video.modules.series.entity.Series;
import com.cms.video.modules.series.mapper.SeriesMapper;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractServiceImpl implements InteractService {

    private final VLikeMapper vLikeMapper;
    private final FavMapper favMapper;
    private final WatchLogMapper watchLogMapper;
    private final VideoMapper videoMapper;
    private final SeriesMapper seriesMapper;
    private final CosService cosService;

    @Override
    @Transactional
    public void like(Long userId, Long videoId) {
        Video video = requireVideo(videoId);
        Long cnt = vLikeMapper.selectCount(new LambdaQueryWrapper<VLike>()
                .eq(VLike::getUserId, userId).eq(VLike::getVideoId, videoId));
        if (cnt != null && cnt > 0) {
            return;
        }
        VLike like = new VLike();
        like.setUserId(userId);
        like.setVideoId(videoId);
        vLikeMapper.insert(like);
        video.setLikeCount((video.getLikeCount() == null ? 0 : video.getLikeCount()) + 1);
        videoMapper.updateById(video);
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long videoId) {
        Video video = requireVideo(videoId);
        int rows = vLikeMapper.delete(new LambdaQueryWrapper<VLike>()
                .eq(VLike::getUserId, userId).eq(VLike::getVideoId, videoId));
        if (rows > 0) {
            long next = Math.max(0, (video.getLikeCount() == null ? 0 : video.getLikeCount()) - 1);
            video.setLikeCount(next);
            videoMapper.updateById(video);
        }
    }

    @Override
    @Transactional
    public void fav(Long userId, Long seriesId) {
        Series series = seriesMapper.selectById(seriesId);
        if (series == null) {
            throw new BizException("合集不存在");
        }
        Long cnt = favMapper.selectCount(new LambdaQueryWrapper<Fav>()
                .eq(Fav::getUserId, userId).eq(Fav::getSeriesId, seriesId));
        if (cnt != null && cnt > 0) {
            return;
        }
        Fav fav = new Fav();
        fav.setUserId(userId);
        fav.setSeriesId(seriesId);
        favMapper.insert(fav);
        series.setFavCount((series.getFavCount() == null ? 0 : series.getFavCount()) + 1);
        seriesMapper.updateById(series);
    }

    @Override
    @Transactional
    public void unfav(Long userId, Long seriesId) {
        Series series = seriesMapper.selectById(seriesId);
        int rows = favMapper.delete(new LambdaQueryWrapper<Fav>()
                .eq(Fav::getUserId, userId).eq(Fav::getSeriesId, seriesId));
        if (rows > 0 && series != null) {
            long next = Math.max(0, (series.getFavCount() == null ? 0 : series.getFavCount()) - 1);
            series.setFavCount(next);
            seriesMapper.updateById(series);
        }
    }

    @Override
    public void reportWatch(Long userId, WatchReq req) {
        Video video = requireVideo(req.getVideoId());
        int progress = Math.max(0, req.getProgress());
        int duration = req.getDuration() == null ? (video.getDuration() == null ? 0 : video.getDuration()) : Math.max(0, req.getDuration());
        if (duration > 0 && video.getDuration() != null && video.getDuration() <= 0) {
            video.setDuration(duration);
            videoMapper.updateById(video);
        }
        WatchLog log = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                .eq(WatchLog::getUserId, userId)
                .eq(WatchLog::getVideoId, video.getId())
                .last("limit 1"));
        if (log == null) {
            log = new WatchLog();
            log.setUserId(userId);
            log.setSeriesId(video.getSeriesId());
            log.setVideoId(video.getId());
            log.setProgress(progress);
            log.setDuration(duration);
            watchLogMapper.insert(log);
        } else {
            if (progress >= (log.getProgress() == null ? 0 : log.getProgress())) {
                log.setProgress(progress);
            }
            if (duration > 0) {
                log.setDuration(duration);
            }
            watchLogMapper.updateById(log);
        }
    }

    @Override
    public PageVo<SeriesVo> myFav(Long userId, long page, long size) {
        Page<Fav> p = favMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Fav>().eq(Fav::getUserId, userId).orderByDesc(Fav::getId));
        List<SeriesVo> list = new ArrayList<>();
        for (Fav fav : p.getRecords()) {
            Series series = seriesMapper.selectById(fav.getSeriesId());
            if (series == null) {
                continue;
            }
            SeriesVo vo = new SeriesVo();
            vo.setId(series.getId());
            vo.setTitle(series.getTitle());
            vo.setCover(series.getCover());
            vo.setIntro(series.getIntro());
            vo.setStatus(series.getStatus());
            vo.setEpCount(series.getEpCount());
            vo.setPlayCount(series.getPlayCount());
            vo.setFavCount(series.getFavCount());
            vo.setFav(true);
            vo.setFirstProgress(calcFirstProgress(userId, series.getId()));
            list.add(vo);
        }
        return PageVo.of(p.getTotal(), list);
    }

    @Override
    public PageVo<SeriesVo> myHistory(Long userId, long page, long size) {
        
        List<WatchLog> logs = watchLogMapper.selectList(new LambdaQueryWrapper<WatchLog>()
                .eq(WatchLog::getUserId, userId)
                .orderByDesc(WatchLog::getUpdatedAt)
                .last("limit 500"));
        Map<Long, WatchLog> latestBySeries = new LinkedHashMap<>();
        for (WatchLog log : logs) {
            Long seriesId = log.getSeriesId();
            if (seriesId == null) {
                Video video = videoMapper.selectById(log.getVideoId());
                if (video != null) {
                    seriesId = video.getSeriesId();
                }
            }
            if (seriesId == null || latestBySeries.containsKey(seriesId)) {
                continue;
            }
            latestBySeries.put(seriesId, log);
        }
        List<Map.Entry<Long, WatchLog>> all = new ArrayList<>(latestBySeries.entrySet());
        long total = all.size();
        int from = (int) Math.max(0, (page - 1) * size);
        int to = (int) Math.min(all.size(), from + size);
        List<SeriesVo> list = new ArrayList<>();
        if (from < to) {
            for (Map.Entry<Long, WatchLog> entry : all.subList(from, to)) {
                Series series = seriesMapper.selectById(entry.getKey());
                if (series == null) {
                    continue;
                }
                WatchLog log = entry.getValue();
                Video video = videoMapper.selectById(log.getVideoId());
                SeriesVo vo = new SeriesVo();
                vo.setId(series.getId());
                vo.setTitle(series.getTitle());
                vo.setCover(series.getCover());
                vo.setIntro(series.getIntro());
                vo.setStatus(series.getStatus());
                vo.setEpCount(series.getEpCount());
                vo.setPlayCount(series.getPlayCount());
                vo.setFavCount(series.getFavCount());
                vo.setLastVideoId(log.getVideoId());
                if (video != null) {
                    vo.setLastEpNo(video.getEpNo());
                    int duration = log.getDuration() != null && log.getDuration() > 0
                            ? log.getDuration()
                            : (video.getDuration() == null ? 0 : video.getDuration());
                    int progress = log.getProgress() == null ? 0 : log.getProgress();
                    vo.setLastProgress(duration > 0
                            ? (int) Math.min(100, Math.round(progress * 100.0 / duration))
                            : 0);
                } else {
                    vo.setLastEpNo(0);
                    vo.setLastProgress(0);
                }
                list.add(vo);
            }
        }
        return PageVo.of(total, list);
    }

    @Override
    public PageVo<VideoVo> myLikes(Long userId, long page, long size) {
        Page<VLike> p = vLikeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<VLike>().eq(VLike::getUserId, userId).orderByDesc(VLike::getId));
        List<VideoVo> list = new ArrayList<>();
        for (VLike like : p.getRecords()) {
            Video video = videoMapper.selectById(like.getVideoId());
            if (video == null) {
                continue;
            }
            VideoVo vo = toVideoVo(video);
            vo.setLiked(true);
            list.add(vo);
        }
        return PageVo.of(p.getTotal(), list);
    }

    private VideoVo toVideoVo(Video video) {
        VideoVo vo = new VideoVo();
        vo.setId(video.getId());
        vo.setSeriesId(video.getSeriesId());
        vo.setTitle(video.getTitle());
        vo.setEpNo(video.getEpNo());
        vo.setCover(video.getCover());
        vo.setDuration(video.getDuration());
        vo.setLikeCount(video.getLikeCount());
        vo.setPlayCount(video.getPlayCount());
        vo.setStatus(video.getStatus());
        vo.setPlayUrl(cosService.buildPlayUrl(video.getCosKey()));
        return vo;
    }

    private int calcFirstProgress(Long userId, Long seriesId) {
        Video first = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getSeriesId, seriesId)
                .eq(Video::getEpNo, 1)
                .last("limit 1"));
        if (first == null || first.getDuration() == null || first.getDuration() <= 0) {
            return 0;
        }
        WatchLog log = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                .eq(WatchLog::getUserId, userId)
                .eq(WatchLog::getVideoId, first.getId())
                .last("limit 1"));
        if (log == null || log.getProgress() == null) {
            return 0;
        }
        return (int) Math.min(100, Math.round(log.getProgress() * 100.0 / first.getDuration()));
    }

    private Video requireVideo(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BizException("视频不存在");
        }
        return video;
    }
}
