package com.cms.video.modules.interact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fav")
public class Fav {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seriesId;
    private LocalDateTime createdAt;
}
