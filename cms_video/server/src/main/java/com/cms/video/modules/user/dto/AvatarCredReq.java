package com.cms.video.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarCredReq {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private String contentType;
}
