package com.cms.video.modules.interact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("watch_log")
public class WatchLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seriesId;
    private Long videoId;
    private Integer progress;
    private Integer duration;
    private LocalDateTime updatedAt;
}
