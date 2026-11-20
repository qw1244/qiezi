package com.cms.video.modules.comment.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.comment.dto.CommentPostReq;
import com.cms.video.modules.comment.service.CommentService;
import com.cms.video.modules.comment.vo.CommentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/video/{videoId}")
    public ApiRes<PageVo<CommentVo>> list(@PathVariable Long videoId,
                                          @RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(commentService.pageByVideo(videoId, page, size, AuthCtx.id()));
    }

    @PostMapping
    public ApiRes<CommentVo> post(@RequestBody CommentPostReq req) {
        return ApiRes.ok(commentService.post(AuthCtx.requireId(), req));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Void> delete(@PathVariable Long id) {
        commentService.delete(AuthCtx.requireId(), id);
        return ApiRes.ok();
    }
}
