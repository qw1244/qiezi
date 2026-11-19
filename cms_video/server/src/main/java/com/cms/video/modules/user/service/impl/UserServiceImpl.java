package com.cms.video.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.video.common.BizException;
import com.cms.video.common.RedisKeys;
import com.cms.video.config.CmsProps;
import com.cms.video.modules.cos.entity.CosCfg;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.user.dto.AvatarCredReq;
import com.cms.video.modules.user.dto.AvatarSaveReq;
import com.cms.video.modules.user.dto.LoginReq;
import com.cms.video.modules.user.dto.PwdReq;
import com.cms.video.modules.user.dto.RegisterReq;
import com.cms.video.modules.user.entity.User;
import com.cms.video.modules.user.mapper.UserMapper;
import com.cms.video.modules.user.service.UserService;
import com.cms.video.modules.user.vo.LoginVo;
import com.cms.video.modules.user.vo.UserVo;
import com.cms.video.security.JwtUtil;
import com.cms.video.security.LoginGuard;
import com.cms.video.security.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int REG_IP_LIMIT = 8;
    private static final Duration REG_IP_WINDOW = Duration.ofMinutes(1);
    private static final Duration REG_USER_LOCK = Duration.ofSeconds(8);
    private static final String LOGIN_SCOPE = "user";
    private static final int LOGIN_FAIL_LIMIT = 8;
    private static final Duration LOGIN_FAIL_WINDOW = Duration.ofMinutes(5);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;
    private final CmsProps cmsProps;
    private final StringRedisTemplate redis;
    private final LoginGuard loginGuard;
    private final CosService cosService;

    
    private final ConcurrentHashMap<String, Object> localLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public LoginVo register(RegisterReq req, String clientIp) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        if (!StringUtils.hasText(username)) {
            throw new BizException("请输入账号");
        }
        String lockKey = username.toLowerCase(Locale.ROOT);
        guardIpRate(clientIp);
        acquireUserLock(lockKey);
        Object local = localLocks.computeIfAbsent(lockKey, k -> new Object());
        synchronized (local) {
            try {
                Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
                if (cnt != null && cnt > 0) {
                    throw new BizException("账号已存在");
                }
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(req.getPassword()));
                String nick = StringUtils.hasText(req.getNickname()) ? req.getNickname().trim() : username;
                user.setNickname(nick);
                user.setAvatar("");
                user.setStatus(1);
                try {
                    userMapper.insert(user);
                } catch (DuplicateKeyException e) {
                    throw new BizException("账号已存在");
                } catch (DataIntegrityViolationException e) {
                    throw new BizException("账号已存在");
                }
                return buildLogin(user);
            } finally {
                localLocks.remove(lockKey, local);
                releaseUserLock(lockKey);
            }
        }
    }

    private void guardIpRate(String clientIp) {
        if (!StringUtils.hasText(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            return;
        }
        String key = RedisKeys.regIp(clientIp);
        Long n = redis.opsForValue().increment(key);
        if (n != null && n == 1L) {
            redis.expire(key, REG_IP_WINDOW);
        }
        if (n != null && n > REG_IP_LIMIT) {
            throw new BizException("注册过于频繁，请稍后再试");
        }
    }

    private void acquireUserLock(String lockKey) {
        String key = RedisKeys.regUser(lockKey);
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", REG_USER_LOCK);
        if (Boolean.FALSE.equals(ok)) {
            throw new BizException("该账号正在注册中，请稍后重试");
        }
    }

    private void releaseUserLock(String lockKey) {
        redis.delete(RedisKeys.regUser(lockKey));
    }

    @Override
    public LoginVo login(LoginReq req, String clientIp) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        loginGuard.assertAllowed(LOGIN_SCOPE, clientIp, username, LOGIN_FAIL_LIMIT);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            loginGuard.onFail(LOGIN_SCOPE, clientIp, username, LOGIN_FAIL_LIMIT, LOGIN_FAIL_WINDOW);
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            loginGuard.onFail(LOGIN_SCOPE, clientIp, username, LOGIN_FAIL_LIMIT, LOGIN_FAIL_WINDOW);
            throw new BizException("账号已禁用");
        }
        
        tokenStore.removeUser(user.getId());
        loginGuard.onSuccess(LOGIN_SCOPE, clientIp, username);
        return buildLogin(user);
    }

    @Override
    public void logout(Long userId) {
        tokenStore.removeUser(userId);
    }

    @Override
    public UserVo profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return toVo(user);
    }

    @Override
    public void changePwd(Long userId, PwdReq req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(req.getOldPwd(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPwd()));
        userMapper.updateById(user);
        tokenStore.removeUser(userId);
    }

    @Override
    public CosCredVo avatarCred(Long userId, AvatarCredReq req) {
        if (userMapper.selectById(userId) == null) {
            throw new BizException("用户不存在");
        }
        String contentType = req.getContentType() == null ? "" : req.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/") || contentType.contains("svg")) {
            throw new BizException("仅支持上传图片头像");
        }
        String folder = "avatar/" + userId;
        return cosService.createPutCred(folder, req.getFileName(), contentType);
    }

    @Override
    public UserVo updateAvatar(Long userId, AvatarSaveReq req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        String avatar = req.getAvatar() == null ? "" : req.getAvatar().trim();
        if (!isAllowedAvatarUrl(avatar, userId)) {
            throw new BizException("头像地址无效");
        }
        user.setAvatar(avatar);
        userMapper.updateById(user);
        return toVo(user);
    }

    private boolean isAllowedAvatarUrl(String avatar, Long userId) {
        if (!StringUtils.hasText(avatar) || avatar.length() > 512) {
            return false;
        }
        if (!(avatar.startsWith("https://") || avatar.startsWith("http://"))) {
            return false;
        }
        if (!avatar.contains("/avatar/" + userId + "/")) {
            return false;
        }
        CosCfg cfg = cosService.getCfg();
        if (!StringUtils.hasText(cfg.getCdnHost())) {
            return false;
        }
        String host = cfg.getCdnHost().trim();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return avatar.startsWith(host + "/");
    }

    private LoginVo buildLogin(User user) {
        String token = jwtUtil.create(user.getId(), JwtUtil.ROLE_USER, cmsProps.getJwt().getUserTtlHours());
        tokenStore.saveUser(user.getId(), token);
        LoginVo vo = new LoginVo();
        vo.setToken(token);
        vo.setUser(toVo(user));
        return vo;
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
