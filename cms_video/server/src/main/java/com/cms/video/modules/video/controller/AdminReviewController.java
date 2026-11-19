package com.cms.video.modules.video.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.PageVo;
import com.cms.video.modules.series.service.SeriesService;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.video.service.VideoService;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final SeriesService seriesService;
    private final VideoService videoService;

    @GetMapping("/series")
    public ApiRes<PageVo<SeriesVo>> pendingSeries(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(seriesService.pendingSeries(page, size));
    }

    @PostMapping("/series/{id}/approve")
    public ApiRes<Void> approveSeries(@PathVariable Long id) {
        seriesService.approveSeries(id);
        return ApiRes.ok();
    }

    @PostMapping("/series/{id}/reject")
    public ApiRes<Void> rejectSeries(@PathVariable Long id) {
        seriesService.rejectSeries(id);
        return ApiRes.ok();
    }

    @GetMapping("/videos")
    public ApiRes<PageVo<VideoVo>> pendingVideos(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(videoService.pendingVideos(page, size));
    }

    @PostMapping("/videos/{id}/approve")
    public ApiRes<Void> approveVideo(@PathVariable Long id) {
        videoService.approve(id);
        return ApiRes.ok();
    }

    @PostMapping("/videos/{id}/reject")
    public ApiRes<Void> rejectVideo(@PathVariable Long id) {
        videoService.reject(id);
        return ApiRes.ok();
    }
}
