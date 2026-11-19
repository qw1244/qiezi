package com.cms.video.modules.admin.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.modules.admin.dto.LoginReq;
import com.cms.video.modules.admin.dto.PwdReq;
import com.cms.video.modules.admin.service.AdminService;
import com.cms.video.modules.admin.vo.AdminVo;
import com.cms.video.modules.admin.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ApiRes<LoginVo> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        return ApiRes.ok(adminService.login(req, clientIp(request)));
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(real)) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/logout")
    public ApiRes<Void> logout() {
        adminService.logout(AuthCtx.requireId());
        return ApiRes.ok();
    }

    @GetMapping("/profile")
    public ApiRes<AdminVo> profile() {
        return ApiRes.ok(adminService.profile(AuthCtx.requireId()));
    }

    @PostMapping("/pwd")
    public ApiRes<Void> pwd(@Valid @RequestBody PwdReq req) {
        adminService.changePwd(AuthCtx.requireId(), req);
        return ApiRes.ok();
    }
}
