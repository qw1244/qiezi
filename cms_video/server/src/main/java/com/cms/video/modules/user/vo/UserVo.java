package com.cms.video.modules.user.vo;

import lombok.Data;

@Data
public class UserVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
}
