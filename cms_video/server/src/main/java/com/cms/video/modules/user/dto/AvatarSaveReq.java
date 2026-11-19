package com.cms.video.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AvatarSaveReq {

    @NotBlank(message = "头像地址不能为空")
    @Size(max = 512, message = "头像地址过长")
    private String avatar;
}
