package com.cms.video.modules.video.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.modules.oplog.service.OpLogService;
import com.cms.video.modules.video.dto.VideoSaveReq;
import com.cms.video.modules.video.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/video")
@RequiredArgsConstructor
public class AdminVideoController {

    private final VideoService videoService;
    private final OpLogService opLogService;

    @PostMapping
    public ApiRes<Map<String, Long>> save(@Valid @RequestBody VideoSaveReq req, HttpServletRequest request) {
        Long id = videoService.save(req);
        opLogService.record(AuthCtx.requireId(), "video.save", "id=" + id, request.getRemoteAddr());
        return ApiRes.ok(Map.of("id", id));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        videoService.remove(id);
        opLogService.record(AuthCtx.requireId(), "video.del", "id=" + id, request.getRemoteAddr());
        return ApiRes.ok();
    }

    @PostMapping("/renumber/{seriesId}")
    public ApiRes<Void> renumber(@PathVariable Long seriesId, HttpServletRequest request) {
        videoService.renumber(seriesId);
        opLogService.record(AuthCtx.requireId(), "video.renumber", "seriesId=" + seriesId, request.getRemoteAddr());
        return ApiRes.ok();
    }
}
