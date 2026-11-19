package com.cms.video.modules.oplog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.PageVo;
import com.cms.video.modules.oplog.entity.OpLog;
import com.cms.video.modules.oplog.mapper.OpLogMapper;
import com.cms.video.modules.oplog.service.OpLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OpLogServiceImpl implements OpLogService {

    
    private static final Set<String> ALLOWED = Set.of(
            "video.save", "video.del", "video.renumber",
            "series.save", "series.del",
            "cos.save", "cos.delete",
            "user.status", "user.resetPwd",
            "oplog.clear",
            "auth.login.ok", "auth.login.fail", "auth.logout", "auth.pwd.change"
    );

    private final OpLogMapper opLogMapper;

    @Override
    public void record(Long adminId, String action, String detail, String ip) {
        if (!StringUtils.hasText(action) || !ALLOWED.contains(action)) {
            return;
        }
        OpLog log = new OpLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setDetail(detail == null ? "" : detail);
        log.setIp(ip == null ? "" : ip);
        opLogMapper.insert(log);
    }

    @Override
    public PageVo<OpLog> page(long page, long size) {
        long p = Math.max(page, 1);
        long s = Math.min(Math.max(size, 1), 100);
        Page<OpLog> result = opLogMapper.selectPage(new Page<>(p, s),
                new LambdaQueryWrapper<OpLog>()
                        .in(OpLog::getAction, ALLOWED)
                        .orderByDesc(OpLog::getId));
        return PageVo.of(result.getTotal(), result.getRecords());
    }

    @Override
    public void clear() {
        opLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
