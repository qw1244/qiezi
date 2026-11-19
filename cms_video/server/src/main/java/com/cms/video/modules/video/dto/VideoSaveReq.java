package com.cms.video.modules.video.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoSaveReq {
    private Long id;
    @NotNull(message = "缺少合集")
    private Long seriesId;
    private String title;
    @NotNull(message = "请输入集数")
    private Integer epNo;
    private String cosKey;
    private String cover;
    private Integer duration;
    private Integer status;
}
