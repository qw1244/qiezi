package com.cms.video.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterReq {

    @NotBlank(message = "请输入账号")
    @Size(min = 6, max = 32, message = "账号长度至少6位")
    @Pattern(regexp = "^(?=.*[a-z]).{6,32}$", message = "账号须包含小写英文字母，长度6-32位")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 8, max = 32, message = "密码长度至少8位")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$",
            message = "密码须包含数字、小写字母和大写字母，长度至少8位"
    )
    private String password;

    private String nickname;
}
