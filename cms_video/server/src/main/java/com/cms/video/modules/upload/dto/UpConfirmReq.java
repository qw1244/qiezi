package com.cms.video.modules.upload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpConfirmReq {

    @NotNull(message = "缺少明细")
    private Long itemId;

    private String cosKey;
    private String cover;
    private Integer duration;
    private Boolean ok;
    private String errMsg;
}
