package com.cms.video.modules.series.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeriesCoverReq {

    @NotBlank(message = "缺少封面地址")
    private String cover;
}
