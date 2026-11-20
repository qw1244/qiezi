package com.cms.video.modules.comment.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVo {
    private Long id;
    private Long videoId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String videoTitle;
    private String content;
    private LocalDateTime createdAt;
    private Boolean mine;
}
