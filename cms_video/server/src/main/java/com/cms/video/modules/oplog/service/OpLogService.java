package com.cms.video.modules.oplog.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.oplog.entity.OpLog;

public interface OpLogService {
    void record(Long adminId, String action, String detail, String ip);

    PageVo<OpLog> page(long page, long size);

    void clear();
}
