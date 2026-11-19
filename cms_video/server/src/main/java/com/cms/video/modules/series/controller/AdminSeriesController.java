package com.cms.video.modules.series.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.oplog.service.OpLogService;
import com.cms.video.modules.series.dto.SeriesSaveReq;
import com.cms.video.modules.series.service.SeriesService;
import com.cms.video.modules.series.vo.SeriesDetailVo;
import com.cms.video.modules.series.vo.SeriesVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/series")
@RequiredArgsConstructor
public class AdminSeriesController {

    private final SeriesService seriesService;
    private final OpLogService opLogService;

    @GetMapping
    public ApiRes<PageVo<SeriesVo>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(seriesService.pageAdmin(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiRes<SeriesDetailVo> detail(@PathVariable Long id) {
        return ApiRes.ok(seriesService.adminDetail(id));
    }

    @PostMapping
    public ApiRes<Map<String, Long>> save(@Valid @RequestBody SeriesSaveReq req, HttpServletRequest request) {
        Long id = seriesService.save(req);
        opLogService.record(AuthCtx.requireId(), "series.save", "id=" + id + ",title=" + req.getTitle(), request.getRemoteAddr());
        return ApiRes.ok(Map.of("id", id));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        seriesService.remove(id);
        opLogService.record(AuthCtx.requireId(), "series.del", "id=" + id, request.getRemoteAddr());
        return ApiRes.ok();
    }
}
