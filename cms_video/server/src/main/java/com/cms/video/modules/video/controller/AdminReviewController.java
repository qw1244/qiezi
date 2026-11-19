package com.cms.video.modules.video.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.PageVo;
import com.cms.video.modules.video.service.VideoService;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final VideoService videoService;

    @GetMapping("/videos")
    public ApiRes<PageVo<VideoVo>> pending(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(videoService.pendingVideos(page, size));
    }

    @PostMapping("/videos/{id}/approve")
    public ApiRes<Void> approve(@PathVariable Long id) {
        videoService.approve(id);
        return ApiRes.ok();
    }

    @PostMapping("/videos/{id}/reject")
    public ApiRes<Void> reject(@PathVariable Long id) {
        videoService.reject(id);
        return ApiRes.ok();
    }
}
