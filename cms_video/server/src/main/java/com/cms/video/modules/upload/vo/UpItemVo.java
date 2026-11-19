package com.cms.video.modules.upload.vo;

import lombok.Data;

@Data
public class UpItemVo {

    private Long id;
    private String fileName;
    private String cosKey;
    private Integer epNo;
    private String title;
    private Integer duration;
    private String status;
    private String errMsg;
    private String putUrl;
}
