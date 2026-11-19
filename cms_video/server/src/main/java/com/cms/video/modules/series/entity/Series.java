package com.cms.video.modules.series.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("series")
public class Series {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String cover;
    private String intro;
    private Integer status;
    private Integer epCount;
    private Long playCount;
    private Long favCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
