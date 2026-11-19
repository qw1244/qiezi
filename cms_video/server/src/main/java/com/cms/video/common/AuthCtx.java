package com.cms.video.common;

public final class AuthCtx {

    private static final ThreadLocal<Long> ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private AuthCtx() {
    }

    public static void set(Long id, String role) {
        ID.set(id);
        ROLE.set(role);
    }

    public static Long id() {
        return ID.get();
    }

    public static String role() {
        return ROLE.get();
    }

    public static Long requireId() {
        Long id = ID.get();
        if (id == null) {
            throw new BizException(401, "未登录");
        }
        return id;
    }

    public static void clear() {
        ID.remove();
        ROLE.remove();
    }
}
