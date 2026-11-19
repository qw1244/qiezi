package com.cms.video.modules.series.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.series.dto.SeriesCoverReq;
import com.cms.video.modules.series.dto.SeriesSaveReq;
import com.cms.video.modules.series.service.SeriesService;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/series")
@RequiredArgsConstructor
public class UserSeriesController {

    private final SeriesService seriesService;
    private final CosService cosService;

    private Long requireUser() {
        Long id = AuthCtx.requireId();
        if (!JwtUtil.ROLE_USER.equals(AuthCtx.role())) {
            throw new BizException(403, "无权限");
        }
        return id;
    }

    @PostMapping
    public ApiRes<Long> create(@Valid @RequestBody SeriesSaveReq req) {
        return ApiRes.ok(seriesService.createByUser(requireUser(), req));
    }

    @GetMapping("/mine")
    public ApiRes<PageVo<SeriesVo>> mine(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(seriesService.mySeries(requireUser(), page, size));
    }

    @PostMapping("/{id}/cover")
    public ApiRes<Void> cover(@PathVariable Long id, @Valid @RequestBody SeriesCoverReq req) {
        seriesService.setCover(requireUser(), id, req.getCover());
        return ApiRes.ok();
    }

    @PostMapping("/cover-cred")
    public ApiRes<CosCredVo> coverCred(@RequestBody Map<String, Object> body) {
        requireUser();
        String fileName = String.valueOf(body.get("fileName"));
        String contentType = body.get("contentType") == null ? null : String.valueOf(body.get("contentType"));
        return ApiRes.ok(cosService.createPutCred("cover", fileName, contentType));
    }
}
