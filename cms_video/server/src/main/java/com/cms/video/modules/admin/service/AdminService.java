package com.cms.video.modules.admin.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.admin.dto.LoginReq;
import com.cms.video.modules.admin.dto.PwdReq;
import com.cms.video.modules.admin.vo.AdminVo;
import com.cms.video.modules.admin.vo.LoginVo;
import com.cms.video.modules.admin.vo.UserDetailVo;
import com.cms.video.modules.user.vo.UserVo;

public interface AdminService {
    LoginVo login(LoginReq req, String clientIp);
    void logout(Long adminId);
    AdminVo profile(Long adminId);
    void changePwd(Long adminId, PwdReq req);
    PageVo<UserVo> pageUsers(String keyword, Integer status, long page, long size);
    void setUserStatus(Long userId, Integer status);
    UserDetailVo userDetail(Long userId);
    String resetUserPwd(Long userId);
}
