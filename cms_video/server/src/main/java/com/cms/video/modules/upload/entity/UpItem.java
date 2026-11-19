package com.cms.video.modules.upload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("up_item")
public class UpItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private String fileName;
    private String cosKey;
    private String cover;
    private Integer epNo;
    private String title;
    private Integer duration;
    private String status;
    private String errMsg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
