package com.cms.video.modules.video.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoVo {
    private Long id;
    private Long seriesId;
    private String seriesTitle;
    private String title;
    private Integer epNo;
    private String cosKey;
    private String cover;
    private Integer duration;
    private Long likeCount;
    private Long playCount;
    private Integer status;
    private String playUrl;
    private Boolean liked;
    private Integer progress;
    private LocalDateTime createdAt;
}
