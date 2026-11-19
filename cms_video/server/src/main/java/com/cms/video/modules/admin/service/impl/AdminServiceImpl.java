package com.cms.video.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.config.CmsProps;
import com.cms.video.modules.admin.dto.LoginReq;
import com.cms.video.modules.admin.dto.PwdReq;
import com.cms.video.modules.admin.entity.Admin;
import com.cms.video.modules.admin.mapper.AdminMapper;
import com.cms.video.modules.admin.service.AdminService;
import com.cms.video.modules.admin.vo.AdminVo;
import com.cms.video.modules.admin.vo.LoginVo;
import com.cms.video.modules.admin.vo.UserDetailVo;
import com.cms.video.modules.interact.service.InteractService;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.user.entity.User;
import com.cms.video.modules.user.mapper.UserMapper;
import com.cms.video.modules.user.vo.UserVo;
import com.cms.video.modules.video.vo.VideoVo;
import com.cms.video.modules.oplog.service.OpLogService;
import com.cms.video.security.JwtUtil;
import com.cms.video.security.LoginGuard;
import com.cms.video.security.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String DEFAULT_RESET_PWD = "123456";
    private static final String WEAK_DEFAULT_PWD = "admin123";
    private static final String SCOPE = "admin";
    private static final int FAIL_LIMIT = 5;
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;
    private final CmsProps cmsProps;
    private final InteractService interactService;
    private final LoginGuard loginGuard;
    private final OpLogService opLogService;

    @Override
    public LoginVo login(LoginReq req, String clientIp) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        loginGuard.assertAllowed(SCOPE, clientIp, username, FAIL_LIMIT);
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null || !passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            opLogService.record(0L, "auth.login.fail", "username=" + username, clientIp);
            loginGuard.onFail(SCOPE, clientIp, username, FAIL_LIMIT, FAIL_WINDOW);
            throw new BizException("账号或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            opLogService.record(admin.getId(), "auth.login.fail", "disabled username=" + username, clientIp);
            loginGuard.onFail(SCOPE, clientIp, username, FAIL_LIMIT, FAIL_WINDOW);
            throw new BizException("账号已禁用");
        }
        
        tokenStore.removeAdmin(admin.getId());
        String token = jwtUtil.create(admin.getId(), JwtUtil.ROLE_ADMIN, cmsProps.getJwt().getAdminTtlHours());
        tokenStore.saveAdmin(admin.getId(), token);
        loginGuard.onSuccess(SCOPE, clientIp, username);
        opLogService.record(admin.getId(), "auth.login.ok", "username=" + username, clientIp);

        LoginVo vo = new LoginVo();
        vo.setToken(token);
        vo.setAdmin(toVo(admin));
        String rawPwd = req.getPassword() == null ? "" : req.getPassword();
        vo.setMustChangePwd(WEAK_DEFAULT_PWD.equals(rawPwd) || DEFAULT_RESET_PWD.equals(rawPwd));
        return vo;
    }

    @Override
    public void logout(Long adminId) {
        tokenStore.removeAdmin(adminId);
        opLogService.record(adminId, "auth.logout", "admin logout", "");
    }

    @Override
    public AdminVo profile(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        return toVo(admin);
    }

    @Override
    public void changePwd(Long adminId, PwdReq req) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        if (!passwordEncoder.matches(req.getOldPwd(), admin.getPassword())) {
            throw new BizException("原密码不正确");
        }
        String newPwd = req.getNewPwd() == null ? "" : req.getNewPwd().trim();
        if (WEAK_DEFAULT_PWD.equals(newPwd) || DEFAULT_RESET_PWD.equals(newPwd)) {
            throw new BizException("请勿使用默认弱密码");
        }
        if (newPwd.length() < 8) {
            throw new BizException("新密码至少 8 位");
        }
        admin.setPassword(passwordEncoder.encode(newPwd));
        adminMapper.updateById(admin);
        tokenStore.removeAdmin(adminId);
        opLogService.record(adminId, "auth.pwd.change", "admin changed password", "");
    }

    @Override
    public PageVo<UserVo> pageUsers(String keyword, Integer status, long page, long size) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (status != null) {
            qw.eq(User::getStatus, status);
        }
        qw.orderByDesc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), qw);
        return PageVo.of(p.getTotal(), p.getRecords().stream().map(this::toUserVo).collect(Collectors.toList()));
    }

    @Override
    public void setUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        if (status != null && status == 0) {
            tokenStore.removeUser(userId);
        }
    }

    @Override
    public UserDetailVo userDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        PageVo<VideoVo> likes = interactService.myLikes(userId, 1, 50);
        PageVo<SeriesVo> favs = interactService.myFav(userId, 1, 50);
        PageVo<SeriesVo> history = interactService.myHistory(userId, 1, 50);
        UserDetailVo vo = new UserDetailVo();
        vo.setUser(toUserVo(user));
        vo.setLikes(likes.getList());
        vo.setFavs(favs.getList());
        vo.setHistory(history.getList());
        vo.setLikeCount(likes.getTotal());
        vo.setFavCount(favs.getTotal());
        vo.setWatchCount(history.getTotal());
        return vo;
    }

    @Override
    public String resetUserPwd(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PWD));
        userMapper.updateById(user);
        tokenStore.removeUser(userId);
        return DEFAULT_RESET_PWD;
    }

    private UserVo toUserVo(User u) {
        UserVo vo = new UserVo();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setStatus(u.getStatus());
        return vo;
    }

    private AdminVo toVo(Admin admin) {
        AdminVo vo = new AdminVo();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        return vo;
    }
}
