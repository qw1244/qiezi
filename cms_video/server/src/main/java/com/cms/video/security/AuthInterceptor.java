package com.cms.video.security;

import com.cms.video.common.AuthCtx;
import com.cms.video.common.BizException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        boolean optional = isOptional(path);
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            if (optional) {
                return true;
            }
            throw new BizException(401, "未登录");
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtUtil.parse(token);
            Long id = Long.valueOf(claims.getSubject());
            String role = String.valueOf(claims.get("role"));
            boolean ok = JwtUtil.ROLE_ADMIN.equals(role)
                    ? tokenStore.matchAdmin(id, token)
                    : tokenStore.matchUser(id, token);
            if (!ok) {
                if (optional) {
                    return true;
                }
                throw new BizException(401, "登录已失效");
            }
            if (path.startsWith("/api/admin") && !JwtUtil.ROLE_ADMIN.equals(role)) {
                throw new BizException(403, "无权限");
            }
            AuthCtx.set(id, role);
            return true;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            if (optional) {
                return true;
            }
            throw new BizException(401, "登录已失效");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthCtx.clear();
    }

    private boolean isOptional(String path) {
        return path.startsWith("/api/open/")
                || path.startsWith("/api/series/")
                || path.startsWith("/api/video/")
                || path.startsWith("/api/comments/video/");
    }
}
