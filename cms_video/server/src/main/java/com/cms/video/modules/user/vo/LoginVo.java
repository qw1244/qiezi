package com.cms.video.modules.user.vo;

import lombok.Data;

@Data
public class LoginVo {

    private String token;
    private UserVo user;
}
