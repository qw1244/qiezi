package com.cms.video.modules.comment.dto;

import lombok.Data;

@Data
public class CommentPostReq {
    private Long videoId;
    private String content;
}
