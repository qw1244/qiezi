package com.cms.video.modules.cos.vo;

import lombok.Data;

@Data
public class CosCredVo {

    private String bucket;
    private String region;
    private String key;
    private String putUrl;
    private String url;
    private String contentType;
    private long expireAt;
}
