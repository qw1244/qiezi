package com.cms.video.modules.upload.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.upload.dto.UpConfirmReq;
import com.cms.video.modules.upload.dto.UpCreateReq;
import com.cms.video.modules.upload.vo.UpJobVo;

public interface UploadService {

    UpJobVo createJob(Long adminId, UpCreateReq req);

    void confirm(UpConfirmReq req);

    UpJobVo detail(Long jobId);

    PageVo<UpJobVo> page(long page, long size);

    void finalizeJob(Long jobId);
}
