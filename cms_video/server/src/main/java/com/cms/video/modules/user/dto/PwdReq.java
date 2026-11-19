package com.cms.video.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PwdReq {

    @NotBlank(message = "请输入原密码")
    private String oldPwd;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度6-32位")
    private String newPwd;
}
