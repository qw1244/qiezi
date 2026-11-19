package com.cms.video.modules.user.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.user.dto.AvatarCredReq;
import com.cms.video.modules.user.dto.AvatarSaveReq;
import com.cms.video.modules.user.dto.LoginReq;
import com.cms.video.modules.user.dto.PwdReq;
import com.cms.video.modules.user.dto.RegisterReq;
import com.cms.video.modules.user.service.UserService;
import com.cms.video.modules.user.vo.LoginVo;
import com.cms.video.modules.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiRes<LoginVo> register(@Valid @RequestBody RegisterReq req, HttpServletRequest request) {
        return ApiRes.ok(userService.register(req, clientIp(request)));
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

    @PostMapping("/login")
    public ApiRes<LoginVo> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        return ApiRes.ok(userService.login(req, clientIp(request)));
    }

    @PostMapping("/logout")
    public ApiRes<Void> logout() {
        userService.logout(AuthCtx.requireId());
        return ApiRes.ok();
    }

    @GetMapping("/profile")
    public ApiRes<UserVo> profile() {
        return ApiRes.ok(userService.profile(AuthCtx.requireId()));
    }

    @PostMapping("/pwd")
    public ApiRes<Void> pwd(@Valid @RequestBody PwdReq req) {
        userService.changePwd(AuthCtx.requireId(), req);
        return ApiRes.ok();
    }

    @PostMapping("/avatar/cred")
    public ApiRes<CosCredVo> avatarCred(@Valid @RequestBody AvatarCredReq req) {
        return ApiRes.ok(userService.avatarCred(AuthCtx.requireId(), req));
    }

    @PostMapping("/avatar")
    public ApiRes<UserVo> avatar(@Valid @RequestBody AvatarSaveReq req) {
        return ApiRes.ok(userService.updateAvatar(AuthCtx.requireId(), req));
    }
}
