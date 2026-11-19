package com.cms.video.modules.stats.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.modules.stats.service.StatsService;
import com.cms.video.modules.stats.vo.DashVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/dash")
    public ApiRes<DashVo> dash() {
        return ApiRes.ok(statsService.dash());
    }
}
