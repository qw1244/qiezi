package com.cms.video.modules.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.interact.entity.VLike;
import com.cms.video.modules.interact.entity.WatchLog;
import com.cms.video.modules.interact.mapper.VLikeMapper;
import com.cms.video.modules.interact.mapper.WatchLogMapper;
import com.cms.video.modules.series.entity.Series;
import com.cms.video.modules.series.mapper.SeriesMapper;
import com.cms.video.modules.video.dto.VideoSaveReq;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import com.cms.video.modules.video.service.VideoService;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final SeriesMapper seriesMapper;
    private final CosService cosService;
    private final VLikeMapper vLikeMapper;
    private final WatchLogMapper watchLogMapper;

    @Override
    public VideoVo play(Long id, Long userId) {
        Video video = videoMapper.selectById(id);
        if (video == null || (video.getStatus() != null && video.getStatus() == 0)) {
            throw new BizException("视频不存在");
        }
        video.setPlayCount((video.getPlayCount() == null ? 0 : video.getPlayCount()) + 1);
        videoMapper.updateById(video);
        Series series = seriesMapper.selectById(video.getSeriesId());
        if (series != null) {
            series.setPlayCount((series.getPlayCount() == null ? 0 : series.getPlayCount()) + 1);
            seriesMapper.updateById(series);
        }
        VideoVo vo = toVo(video);
        if (series != null) {
            vo.setSeriesTitle(series.getTitle());
        }
        if (userId != null) {
            Long liked = vLikeMapper.selectCount(new LambdaQueryWrapper<VLike>()
                    .eq(VLike::getUserId, userId).eq(VLike::getVideoId, id));
            vo.setLiked(liked != null && liked > 0);
            WatchLog log = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                    .eq(WatchLog::getUserId, userId).eq(WatchLog::getVideoId, id).last("limit 1"));
            vo.setProgress(log == null || log.getProgress() == null ? 0 : log.getProgress());
        } else {
            vo.setLiked(false);
            vo.setProgress(0);
        }
        return vo;
    }

    @Override
    @Transactional
    public Long save(VideoSaveReq req) {
        Series series = seriesMapper.selectById(req.getSeriesId());
        if (series == null) {
            throw new BizException("合集不存在");
        }
        Video video;
        if (req.getId() == null) {
            Video exist = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                    .eq(Video::getSeriesId, req.getSeriesId())
                    .eq(Video::getEpNo, req.getEpNo())
                    .last("limit 1"));
            if (exist != null) {
                video = exist;
            } else {
                video = new Video();
                video.setLikeCount(0L);
                video.setPlayCount(0L);
            }
        } else {
            video = videoMapper.selectById(req.getId());
            if (video == null) {
                throw new BizException("视频不存在");
            }
        }

        Integer oldEp = video.getEpNo();
        Integer newEp = req.getEpNo();
        if (video.getId() != null && newEp != null && oldEp != null && !newEp.equals(oldEp)) {
            Video conflict = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                    .eq(Video::getSeriesId, req.getSeriesId())
                    .eq(Video::getEpNo, newEp)
                    .ne(Video::getId, video.getId())
                    .last("limit 1"));
            if (conflict != null) {
                int tempEp = -Math.toIntExact(video.getId());
                conflict.setEpNo(tempEp);
                videoMapper.updateById(conflict);
                video.setEpNo(newEp);
                applyFields(video, req, false);
                videoMapper.updateById(video);
                conflict.setEpNo(oldEp);
                videoMapper.updateById(conflict);
                refreshEpCount(req.getSeriesId());
                return video.getId();
            }
        }

        applyFields(video, req, video.getId() == null || !StringUtils.hasText(video.getTitle()));
        video.setEpNo(newEp);
        if (video.getId() == null) {
            videoMapper.insert(video);
        } else {
            videoMapper.updateById(video);
        }
        refreshEpCount(req.getSeriesId());
        return video.getId();
    }

    private void applyFields(Video video, VideoSaveReq req, boolean fillDefaultTitle) {
        video.setSeriesId(req.getSeriesId());
        if (StringUtils.hasText(req.getTitle())) {
            video.setTitle(req.getTitle().trim());
        } else if (fillDefaultTitle) {
            video.setTitle("第" + req.getEpNo() + "集");
        }
        if (req.getCosKey() != null) {
            video.setCosKey(req.getCosKey());
        }
        if (req.getCover() != null) {
            video.setCover(req.getCover());
        }
        if (req.getDuration() != null) {
            video.setDuration(req.getDuration());
        }
        video.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    }

    @Override
    public PageVo<VideoVo> pendingVideos(long page, long size) {
        // 只列已上架合集里的待审核视频；待审核合集里的视频走合集审核，不在这里重复出现
        Page<Video> p = videoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getStatus, 0)
                        .inSql(Video::getSeriesId, "SELECT id FROM series WHERE status = 1")
                        .orderByDesc(Video::getCreatedAt));
        List<VideoVo> list = p.getRecords().stream().map(v -> {
            VideoVo vo = toVo(v);
            Series s = seriesMapper.selectById(v.getSeriesId());
            if (s != null) {
                vo.setSeriesTitle(s.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageVo.of(p.getTotal(), list);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new BizException("视频不存在");
        }
        if (video.getStatus() == null || video.getStatus() != 0) {
            throw new BizException("该视频不是待审核状态");
        }
        video.setStatus(1);
        videoMapper.updateById(video);
        refreshEpCount(video.getSeriesId());
    }

    @Override
    @Transactional
    public void reject(Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new BizException("视频不存在");
        }
        if (video.getStatus() == null || video.getStatus() != 0) {
            throw new BizException("该视频不是待审核状态");
        }
        remove(id);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            return;
        }
        if (StringUtils.hasText(video.getCosKey())) {
            try {
                cosService.deleteObject(video.getCosKey());
            } catch (Exception ignored) {
            }
        }
        Long seriesId = video.getSeriesId();
        videoMapper.deleteById(id);
        vLikeMapper.delete(new LambdaQueryWrapper<VLike>().eq(VLike::getVideoId, id));
        watchLogMapper.delete(new LambdaQueryWrapper<WatchLog>().eq(WatchLog::getVideoId, id));
        
        renumberEpisodes(seriesId);
    }

    @Override
    @Transactional
    public void renumber(Long seriesId) {
        Series series = seriesMapper.selectById(seriesId);
        if (series == null) {
            throw new BizException("合集不存在");
        }
        renumberEpisodes(seriesId);
    }

    private void renumberEpisodes(Long seriesId) {
        List<Video> list = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getSeriesId, seriesId)
                .orderByAsc(Video::getEpNo)
                .orderByAsc(Video::getId));
        if (list.isEmpty()) {
            refreshEpCount(seriesId);
            return;
        }
        for (Video v : list) {
            v.setEpNo(-Math.toIntExact(v.getId()));
            videoMapper.updateById(v);
        }
        int ep = 1;
        for (Video v : list) {
            v.setEpNo(ep);
            if (v.getTitle() != null && v.getTitle().matches("第\\d+集")) {
                v.setTitle("第" + ep + "集");
            }
            videoMapper.updateById(v);
            ep++;
        }
        refreshEpCount(seriesId);
    }

    private void refreshEpCount(Long seriesId) {
        Long cnt = videoMapper.selectCount(new LambdaQueryWrapper<Video>().eq(Video::getSeriesId, seriesId));
        Series series = seriesMapper.selectById(seriesId);
        if (series != null) {
            series.setEpCount(cnt == null ? 0 : cnt.intValue());
            seriesMapper.updateById(series);
        }
    }

    private VideoVo toVo(Video v) {
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
