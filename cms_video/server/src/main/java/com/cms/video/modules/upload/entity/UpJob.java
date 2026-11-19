package com.cms.video.modules.upload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("up_job")
public class UpJob {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private Long userId;
    private Long seriesId;
    private Integer total;
    private Integer okNum;
    private Integer failNum;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
