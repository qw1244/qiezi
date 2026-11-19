package com.cms.video.modules.upload.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.common.RedisKeys;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.series.entity.Series;
import com.cms.video.modules.series.mapper.SeriesMapper;
import com.cms.video.modules.upload.dto.UpConfirmReq;
import com.cms.video.modules.upload.dto.UpCreateReq;
import com.cms.video.modules.upload.dto.UpFileReq;
import com.cms.video.modules.upload.entity.UpItem;
import com.cms.video.modules.upload.entity.UpJob;
import com.cms.video.modules.upload.mapper.UpItemMapper;
import com.cms.video.modules.upload.mapper.UpJobMapper;
import com.cms.video.modules.upload.service.UploadService;
import com.cms.video.modules.upload.vo.UpItemVo;
import com.cms.video.modules.upload.vo.UpJobVo;
import com.cms.video.modules.video.dto.VideoSaveReq;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import com.cms.video.modules.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final UpJobMapper upJobMapper;
    private final UpItemMapper upItemMapper;
    private final SeriesMapper seriesMapper;
    private final VideoMapper videoMapper;
    private final CosService cosService;
    private final VideoService videoService;
    private final StringRedisTemplate redis;

    @Override
    @Transactional
    public UpJobVo createJob(Long adminId, UpCreateReq req) {
        return doCreateJob(adminId, null, req);
    }

    @Override
    @Transactional
    public UpJobVo createUserJob(Long userId, UpCreateReq req) {
        Series series = seriesMapper.selectById(req.getSeriesId());
        if (series == null) {
            throw new BizException("合集不存在");
        }
        if (series.getUserId() == null || !series.getUserId().equals(userId)) {
            throw new BizException(403, "只能上传到自己新建的合集");
        }
        return doCreateJob(0L, userId, req);
    }

    private UpJobVo doCreateJob(Long adminId, Long userId, UpCreateReq req) {
        Series series = seriesMapper.selectById(req.getSeriesId());
        if (series == null) {
            throw new BizException("合集不存在");
        }
        if (!cosService.ready()) {
            throw new BizException("请先配置并启用 R2 存储");
        }
        UpJob job = new UpJob();
        job.setAdminId(adminId);
        job.setUserId(userId);
        job.setSeriesId(req.getSeriesId());
        job.setTotal(req.getFiles().size());
        job.setOkNum(0);
        job.setFailNum(0);
        job.setStatus("uploading");
        upJobMapper.insert(job);

        List<UpItemVo> itemVos = new ArrayList<>();
        int autoEp = nextEp(req.getSeriesId());
        java.util.HashSet<Integer> usedEp = new java.util.HashSet<>();
        for (UpFileReq file : req.getFiles()) {
            int epNo;
            if (file.getEpNo() != null && file.getEpNo() > 0 && usedEp.add(file.getEpNo())) {
                epNo = file.getEpNo();
                if (epNo >= autoEp) {
                    autoEp = epNo + 1;
                }
            } else {
                while (!usedEp.add(autoEp)) {
                    autoEp++;
                }
                epNo = autoEp++;
            }
            UpItem item = new UpItem();
            item.setJobId(job.getId());
            item.setFileName(file.getFileName() == null ? "" : file.getFileName());
            item.setEpNo(epNo);
            item.setTitle(StringUtils.hasText(file.getTitle()) ? file.getTitle().trim() : ("第" + epNo + "集"));
            item.setDuration(file.getDuration() == null ? 0 : file.getDuration());
            item.setStatus("pending");
            item.setErrMsg("");
            item.setCosKey("");
            item.setCover("");
            upItemMapper.insert(item);

            CosCredVo cred = cosService.createPutCred(req.getSeriesId(), item.getFileName());
            item.setCosKey(cred.getKey());
            upItemMapper.updateById(item);

            UpItemVo vo = toItemVo(item);
            vo.setPutUrl(cred.getPutUrl());
            itemVos.add(vo);
        }
        return toJobVo(job, itemVos);
    }

    @Override
    @Transactional
    public void confirm(UpConfirmReq req) {
        UpItem item = upItemMapper.selectById(req.getItemId());
        if (item == null) {
            throw new BizException("上传明细不存在");
        }
        boolean ok = req.getOk() == null || Boolean.TRUE.equals(req.getOk());
        if (ok) {
            if (StringUtils.hasText(req.getCosKey())) {
                item.setCosKey(req.getCosKey());
            }
            if (req.getDuration() != null) {
                item.setDuration(req.getDuration());
            }
            if (StringUtils.hasText(req.getCover())) {
                item.setCover(req.getCover());
            }
            item.setStatus("ok");
            item.setErrMsg("");
        } else {
            item.setStatus("fail");
            item.setErrMsg(req.getErrMsg() == null ? "上传失败" : req.getErrMsg());
        }
        upItemMapper.updateById(item);

        UpJob job = upJobMapper.selectById(item.getJobId());
        if (job == null) {
            return;
        }
        refreshJobCount(job);
        if (isFinished(job)) {
            job.setStatus("queued");
            upJobMapper.updateById(job);
            redis.opsForList().leftPush(RedisKeys.UP_QUEUE, String.valueOf(job.getId()));
        } else {
            upJobMapper.updateById(job);
        }
    }

    @Override
    public UpJobVo detail(Long jobId) {
        UpJob job = upJobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException("任务不存在");
        }
        List<UpItem> items = upItemMapper.selectList(new LambdaQueryWrapper<UpItem>()
                .eq(UpItem::getJobId, jobId)
                .orderByAsc(UpItem::getEpNo));
        return toJobVo(job, items.stream().map(this::toItemVo).collect(Collectors.toList()));
    }

    @Override
    public PageVo<UpJobVo> page(long page, long size) {
        Page<UpJob> p = upJobMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UpJob>().orderByDesc(UpJob::getId));
        List<UpJobVo> list = p.getRecords().stream().map(j -> toJobVo(j, null)).collect(Collectors.toList());
        return PageVo.of(p.getTotal(), list);
    }

    @Override
    public void userConfirm(Long userId, UpConfirmReq req) {
        UpItem item = upItemMapper.selectById(req.getItemId());
        if (item == null) {
            throw new BizException("上传明细不存在");
        }
        UpJob job = upJobMapper.selectById(item.getJobId());
        if (job == null || job.getUserId() == null || !job.getUserId().equals(userId)) {
            throw new BizException(403, "无权限");
        }
        confirm(req);
    }

    @Override
    public UpJobVo myJobDetail(Long userId, Long jobId) {
        UpJob job = upJobMapper.selectById(jobId);
        if (job == null || job.getUserId() == null || !job.getUserId().equals(userId)) {
            throw new BizException("任务不存在");
        }
        return detail(jobId);
    }

    @Override
    public PageVo<UpJobVo> myJobs(Long userId, long page, long size) {
        Page<UpJob> p = upJobMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UpJob>().eq(UpJob::getUserId, userId).orderByDesc(UpJob::getId));
        List<UpJobVo> list = p.getRecords().stream().map(j -> toJobVo(j, null)).collect(Collectors.toList());
        return PageVo.of(p.getTotal(), list);
    }

    @Override
    @Transactional
    public void finalizeJob(Long jobId) {
        UpJob job = upJobMapper.selectById(jobId);
        if (job == null) {
            return;
        }
        job.setStatus("running");
        upJobMapper.updateById(job);
        List<UpItem> items = upItemMapper.selectList(new LambdaQueryWrapper<UpItem>()
                .eq(UpItem::getJobId, jobId)
                .eq(UpItem::getStatus, "ok"));
        for (UpItem item : items) {
            int epNo = item.getEpNo() == null ? nextEp(job.getSeriesId()) : item.getEpNo();
            Video exist = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                    .eq(Video::getSeriesId, job.getSeriesId())
                    .eq(Video::getEpNo, epNo)
                    .last("limit 1"));
            if (exist != null && StringUtils.hasText(exist.getCosKey())
                    && !exist.getCosKey().equals(item.getCosKey())) {
                epNo = nextEp(job.getSeriesId());
                item.setEpNo(epNo);
                item.setTitle("第" + epNo + "集");
                upItemMapper.updateById(item);
            }
            VideoSaveReq saveReq = new VideoSaveReq();
            saveReq.setSeriesId(job.getSeriesId());
            saveReq.setTitle(item.getTitle());
            saveReq.setEpNo(epNo);
            saveReq.setCosKey(item.getCosKey());
            saveReq.setCover(item.getCover());
            saveReq.setDuration(item.getDuration());
            // 管理员上传直接上架；用户投稿的视频一律先待审核，等管理员通过
            saveReq.setStatus(job.getUserId() != null ? 0 : 1);
            videoService.save(saveReq);
        }
        refreshJobCount(job);
        job.setStatus("done");
        upJobMapper.updateById(job);
    }

    private int nextEp(Long seriesId) {
        Video last = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                        .eq(Video::getSeriesId, seriesId))
                .stream()
                .max(Comparator.comparing(v -> v.getEpNo() == null ? 0 : v.getEpNo()))
                .orElse(null);
        if (last == null || last.getEpNo() == null) {
            return 1;
        }
        return last.getEpNo() + 1;
    }

    private void refreshJobCount(UpJob job) {
        Long ok = upItemMapper.selectCount(new LambdaQueryWrapper<UpItem>()
                .eq(UpItem::getJobId, job.getId()).eq(UpItem::getStatus, "ok"));
        Long fail = upItemMapper.selectCount(new LambdaQueryWrapper<UpItem>()
                .eq(UpItem::getJobId, job.getId()).eq(UpItem::getStatus, "fail"));
        job.setOkNum(ok == null ? 0 : ok.intValue());
        job.setFailNum(fail == null ? 0 : fail.intValue());
    }

    private boolean isFinished(UpJob job) {
        Long pending = upItemMapper.selectCount(new LambdaQueryWrapper<UpItem>()
                .eq(UpItem::getJobId, job.getId()).eq(UpItem::getStatus, "pending"));
        return pending == null || pending == 0;
    }

    private UpJobVo toJobVo(UpJob job, List<UpItemVo> items) {
        UpJobVo vo = new UpJobVo();
        vo.setId(job.getId());
        vo.setSeriesId(job.getSeriesId());
        vo.setTotal(job.getTotal());
        vo.setOkNum(job.getOkNum());
        vo.setFailNum(job.getFailNum());
        vo.setStatus(job.getStatus());
        vo.setCreatedAt(job.getCreatedAt());
        vo.setItems(items);
        return vo;
    }

    private UpItemVo toItemVo(UpItem item) {
        UpItemVo vo = new UpItemVo();
        vo.setId(item.getId());
        vo.setFileName(item.getFileName());
        vo.setCosKey(item.getCosKey());
        vo.setEpNo(item.getEpNo());
        vo.setTitle(item.getTitle());
        vo.setDuration(item.getDuration());
        vo.setStatus(item.getStatus());
        vo.setErrMsg(item.getErrMsg());
        return vo;
    }
}
