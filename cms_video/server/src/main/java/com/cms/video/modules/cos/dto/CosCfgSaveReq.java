package com.cms.video.modules.cos.dto;

import lombok.Data;

@Data
public class CosCfgSaveReq {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String cdnHost;
    private String pathPrefix;
    private Integer enabled;
}
