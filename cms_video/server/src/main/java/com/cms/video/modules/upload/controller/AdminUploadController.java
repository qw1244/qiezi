package com.cms.video.modules.upload.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.upload.dto.UpConfirmReq;
import com.cms.video.modules.upload.dto.UpCreateReq;
import com.cms.video.modules.upload.service.UploadService;
import com.cms.video.modules.upload.vo.UpJobVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final UploadService uploadService;

    @PostMapping("/job")
    public ApiRes<UpJobVo> create(@Valid @RequestBody UpCreateReq req) {
        UpJobVo vo = uploadService.createJob(AuthCtx.requireId(), req);
        return ApiRes.ok(vo);
    }

    @PostMapping("/confirm")
    public ApiRes<Void> confirm(@Valid @RequestBody UpConfirmReq req) {
        uploadService.confirm(req);
        return ApiRes.ok();
    }

    @GetMapping("/job/{id}")
    public ApiRes<UpJobVo> detail(@PathVariable Long id) {
        return ApiRes.ok(uploadService.detail(id));
    }

    @GetMapping("/job")
    public ApiRes<PageVo<UpJobVo>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(uploadService.page(page, size));
    }
}
