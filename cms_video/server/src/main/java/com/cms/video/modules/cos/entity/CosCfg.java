package com.cms.video.modules.cos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cos_cfg")
public class CosCfg {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String cdnHost;
    private String pathPrefix;
    private Integer enabled;
    private LocalDateTime updatedAt;
}
