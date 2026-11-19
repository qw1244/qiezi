package com.cms.video.modules.admin.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.common.PageVo;
import com.cms.video.modules.admin.service.AdminService;
import com.cms.video.modules.admin.vo.UserDetailVo;
import com.cms.video.modules.oplog.service.OpLogService;
import com.cms.video.modules.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;
    private final OpLogService opLogService;

    @GetMapping
    public ApiRes<PageVo<UserVo>> page(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size) {
        return ApiRes.ok(adminService.pageUsers(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiRes<UserDetailVo> detail(@PathVariable Long id) {
        return ApiRes.ok(adminService.userDetail(id));
    }

    @PostMapping("/{id}/status")
    public ApiRes<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body, HttpServletRequest request) {
        Integer status = body.get("status");
        adminService.setUserStatus(id, status);
        opLogService.record(AuthCtx.requireId(), "user.status", "userId=" + id + ",status=" + status, request.getRemoteAddr());
        return ApiRes.ok();
    }

    @PostMapping("/{id}/reset-pwd")
    public ApiRes<Map<String, String>> resetPwd(@PathVariable Long id, HttpServletRequest request) {
        String pwd = adminService.resetUserPwd(id);
        opLogService.record(AuthCtx.requireId(), "user.resetPwd", "userId=" + id, request.getRemoteAddr());
        return ApiRes.ok(Map.of("password", pwd));
    }
}
