package com.cms.video.modules.user.service;

import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.user.dto.AvatarCredReq;
import com.cms.video.modules.user.dto.AvatarSaveReq;
import com.cms.video.modules.user.dto.LoginReq;
import com.cms.video.modules.user.dto.PwdReq;
import com.cms.video.modules.user.dto.RegisterReq;
import com.cms.video.modules.user.vo.LoginVo;
import com.cms.video.modules.user.vo.UserVo;

public interface UserService {

    LoginVo register(RegisterReq req, String clientIp);

    LoginVo login(LoginReq req, String clientIp);

    void logout(Long userId);

    UserVo profile(Long userId);

    void changePwd(Long userId, PwdReq req);

    CosCredVo avatarCred(Long userId, AvatarCredReq req);

    UserVo updateAvatar(Long userId, AvatarSaveReq req);
}
