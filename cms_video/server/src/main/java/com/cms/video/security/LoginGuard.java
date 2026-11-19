package com.cms.video.security;

import com.cms.video.common.BizException;
import com.cms.video.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LoginGuard {

    private final StringRedisTemplate redis;

    
    public void assertAllowed(String scope, String clientIp, String username, int limit) {
        String user = normUser(username);
        String ip = normIp(clientIp);
        if (over(RedisKeys.loginFailCombo(scope, ip, user), limit)
                || over(RedisKeys.loginFailIp(scope, ip), limit)
                || over(RedisKeys.loginFailUser(scope, user), limit)) {
            throw new BizException("登录失败次数过多，请稍后再试");
        }
    }

    
    public void onFail(String scope, String clientIp, String username, int limit, Duration window) {
        String user = normUser(username);
        String ip = normIp(clientIp);
        bump(RedisKeys.loginFailCombo(scope, ip, user), window);
        bump(RedisKeys.loginFailIp(scope, ip), window);
        bump(RedisKeys.loginFailUser(scope, user), window);
        assertAllowed(scope, clientIp, username, limit);
    }

    
    public void onSuccess(String scope, String clientIp, String username) {
        String user = normUser(username);
        String ip = normIp(clientIp);
        redis.delete(RedisKeys.loginFailCombo(scope, ip, user));
        redis.delete(RedisKeys.loginFailIp(scope, ip));
        redis.delete(RedisKeys.loginFailUser(scope, user));
    }

    private boolean over(String key, int limit) {
        String v = redis.opsForValue().get(key);
        if (!StringUtils.hasText(v)) {
            return false;
        }
        try {
            return Long.parseLong(v) >= limit;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void bump(String key, Duration window) {
        Long n = redis.opsForValue().increment(key);
        if (n != null && n == 1L) {
            redis.expire(key, window);
        }
    }

    private String normUser(String username) {
        return StringUtils.hasText(username) ? username.trim().toLowerCase(Locale.ROOT) : "-";
    }

    private String normIp(String clientIp) {
        return StringUtils.hasText(clientIp) ? clientIp.trim() : "unknown";
    }
}