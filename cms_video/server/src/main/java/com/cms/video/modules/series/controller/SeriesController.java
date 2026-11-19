package com.cms.video.modules.series.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.series.service.SeriesService;
import com.cms.video.modules.series.vo.SeriesDetailVo;
import com.cms.video.modules.series.vo.SeriesVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping("/list")
    public ApiRes<PageVo<SeriesVo>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "12") long size) {
        return ApiRes.ok(seriesService.pageFront(keyword, page, size, AuthCtx.id()));
    }

    @GetMapping("/{id}")
    public ApiRes<SeriesDetailVo> detail(@PathVariable Long id) {
        return ApiRes.ok(seriesService.detail(id, AuthCtx.id()));
    }
}
