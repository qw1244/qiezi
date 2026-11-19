package com.cms.video.modules.upload.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.upload.dto.UpConfirmReq;
import com.cms.video.modules.upload.dto.UpCreateReq;
import com.cms.video.modules.upload.vo.UpJobVo;

public interface UploadService {

    UpJobVo createJob(Long adminId, UpCreateReq req);

    UpJobVo createUserJob(Long userId, UpCreateReq req);

    void confirm(UpConfirmReq req);

    void userConfirm(Long userId, UpConfirmReq req);

    UpJobVo detail(Long jobId);

    UpJobVo myJobDetail(Long userId, Long jobId);

    PageVo<UpJobVo> page(long page, long size);

    PageVo<UpJobVo> myJobs(Long userId, long page, long size);

    void finalizeJob(Long jobId);
}
