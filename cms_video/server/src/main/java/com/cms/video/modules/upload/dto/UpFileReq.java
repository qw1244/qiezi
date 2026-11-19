package com.cms.video.modules.upload.dto;

import lombok.Data;

@Data
public class UpFileReq {

    private String fileName;
    private Integer epNo;
    private String title;
    private Integer duration;
}
