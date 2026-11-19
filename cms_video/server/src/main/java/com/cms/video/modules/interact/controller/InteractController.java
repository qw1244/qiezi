package com.cms.video.modules.interact.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.interact.dto.WatchReq;
import com.cms.video.modules.interact.service.InteractService;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.video.vo.VideoVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interact")
@RequiredArgsConstructor
public class InteractController {

    private final InteractService interactService;

    @PostMapping("/like/{videoId}")
    public ApiRes<Void> like(@PathVariable Long videoId) {
        interactService.like(AuthCtx.requireId(), videoId);
        return ApiRes.ok();
    }

    @DeleteMapping("/like/{videoId}")
    public ApiRes<Void> unlike(@PathVariable Long videoId) {
        interactService.unlike(AuthCtx.requireId(), videoId);
        return ApiRes.ok();
    }

    @PostMapping("/fav/{seriesId}")
    public ApiRes<Void> fav(@PathVariable Long seriesId) {
        interactService.fav(AuthCtx.requireId(), seriesId);
        return ApiRes.ok();
    }

    @DeleteMapping("/fav/{seriesId}")
    public ApiRes<Void> unfav(@PathVariable Long seriesId) {
        interactService.unfav(AuthCtx.requireId(), seriesId);
        return ApiRes.ok();
    }

    @PostMapping("/watch")
    public ApiRes<Void> watch(@Valid @RequestBody WatchReq req) {
        interactService.reportWatch(AuthCtx.requireId(), req);
        return ApiRes.ok();
    }

    @GetMapping("/fav")
    public ApiRes<PageVo<SeriesVo>> myFav(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "12") long size) {
        return ApiRes.ok(interactService.myFav(AuthCtx.requireId(), page, size));
    }

    @GetMapping("/history")
    public ApiRes<PageVo<SeriesVo>> history(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size) {
        return ApiRes.ok(interactService.myHistory(AuthCtx.requireId(), page, size));
    }
}
