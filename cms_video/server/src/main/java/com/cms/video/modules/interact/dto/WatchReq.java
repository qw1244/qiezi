package com.cms.video.modules.interact.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WatchReq {

    @NotNull(message = "缺少视频")
    private Long videoId;

    @NotNull(message = "缺少进度")
    private Integer progress;

    private Integer duration;
}
