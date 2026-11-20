package com.cms.video.modules.comment.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.PageVo;
import com.cms.video.modules.comment.service.CommentService;
import com.cms.video.modules.comment.vo.CommentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiRes<PageVo<CommentVo>> list(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(commentService.pageAdmin(page, size));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Void> delete(@PathVariable Long id) {
        commentService.deleteByAdmin(id);
        return ApiRes.ok();
    }
}
