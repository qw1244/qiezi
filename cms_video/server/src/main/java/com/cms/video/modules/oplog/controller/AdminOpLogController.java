package com.cms.video.modules.oplog.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.oplog.entity.OpLog;
import com.cms.video.modules.oplog.service.OpLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/oplog")
@RequiredArgsConstructor
public class AdminOpLogController {

    private final OpLogService opLogService;

    @GetMapping
    public ApiRes<PageVo<OpLog>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size) {
        return ApiRes.ok(opLogService.page(page, size));
    }

    @DeleteMapping
    public ApiRes<Void> clear(HttpServletRequest request) {
        opLogService.clear();
        
        opLogService.record(AuthCtx.requireId(), "oplog.clear", "清空操作记录", request.getRemoteAddr());
        return ApiRes.ok();
    }
}
