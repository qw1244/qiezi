package com.cms.video.modules.admin.vo;

import lombok.Data;

@Data
public class LoginVo {
    private String token;
    private AdminVo admin;
    
    private Boolean mustChangePwd;
}
