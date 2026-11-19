package com.cms.video.modules.upload.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.upload.dto.UpConfirmReq;
import com.cms.video.modules.upload.dto.UpCreateReq;
import com.cms.video.modules.upload.service.UploadService;
import com.cms.video.modules.upload.vo.UpJobVo;
import com.cms.video.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UserUploadController {

    private final UploadService uploadService;

    private Long requireUser() {
        Long id = AuthCtx.requireId();
        if (!JwtUtil.ROLE_USER.equals(AuthCtx.role())) {
            throw new BizException(403, "无权限");
        }
        return id;
    }

    @PostMapping("/job")
    public ApiRes<UpJobVo> create(@Valid @RequestBody UpCreateReq req) {
        return ApiRes.ok(uploadService.createUserJob(requireUser(), req));
    }

    @PostMapping("/confirm")
    public ApiRes<Void> confirm(@Valid @RequestBody UpConfirmReq req) {
        uploadService.userConfirm(requireUser(), req);
        return ApiRes.ok();
    }

    @GetMapping("/job/{id}")
    public ApiRes<UpJobVo> detail(@PathVariable Long id) {
        return ApiRes.ok(uploadService.myJobDetail(requireUser(), id));
    }

    @GetMapping("/job")
    public ApiRes<PageVo<UpJobVo>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(uploadService.myJobs(requireUser(), page, size));
    }
}
