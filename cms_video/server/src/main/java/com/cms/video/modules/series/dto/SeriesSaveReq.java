package com.cms.video.modules.series.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeriesSaveReq {
    private Long id;
    @NotBlank(message = "请输入标题")
    private String title;
    private String cover;
    private String intro;
    private Integer status;
}
