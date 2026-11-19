package com.cms.video.security;

import com.cms.video.common.RedisKeys;
import com.cms.video.config.CmsProps;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenStore {

    private final StringRedisTemplate redis;
    private final CmsProps cmsProps;

    public void saveUser(Long userId, String token) {
        redis.opsForValue().set(
                RedisKeys.userToken(userId),
                token,
                Duration.ofHours(cmsProps.getJwt().getUserTtlHours())
        );
    }

    public void saveAdmin(Long adminId, String token) {
        redis.opsForValue().set(
                RedisKeys.adminToken(adminId),
                token,
                Duration.ofHours(cmsProps.getJwt().getAdminTtlHours())
        );
    }

    public boolean matchUser(Long userId, String token) {
        String cached = redis.opsForValue().get(RedisKeys.userToken(userId));
        return token != null && token.equals(cached);
    }

    public boolean matchAdmin(Long adminId, String token) {
        String cached = redis.opsForValue().get(RedisKeys.adminToken(adminId));
        return token != null && token.equals(cached);
    }

    public void removeUser(Long userId) {
        redis.delete(RedisKeys.userToken(userId));
    }

    public void removeAdmin(Long adminId) {
        redis.delete(RedisKeys.adminToken(adminId));
    }
}
