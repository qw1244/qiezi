package com.cms.video.common;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static String userToken(Long userId) {
        return "cms:tok:u:" + userId;
    }

    public static String adminToken(Long adminId) {
        return "cms:tok:a:" + adminId;
    }

    public static final String UP_QUEUE = "cms:up:queue";

    public static String regIp(String ip) {
        return "cms:reg:ip:" + ip;
    }

    public static String regUser(String username) {
        return "cms:reg:u:" + username;
    }

    public static String loginFailIp(String scope, String ip) {
        return "cms:login:fail:ip:" + scope + ":" + ip;
    }

    public static String loginFailUser(String scope, String username) {
        return "cms:login:fail:u:" + scope + ":" + username;
    }

    public static String loginFailCombo(String scope, String ip, String username) {
        return "cms:login:fail:c:" + scope + ":" + ip + ":" + username;
    }
}
