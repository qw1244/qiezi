package com.cms.video.modules.video.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.modules.video.service.VideoService;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("/play/{id}")
    public ApiRes<VideoVo> play(@PathVariable Long id) {
        return ApiRes.ok(videoService.play(id, AuthCtx.id()));
    }
}
