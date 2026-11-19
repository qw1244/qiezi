package com.cms.video.modules.upload.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpJobVo {

    private Long id;
    private Long seriesId;
    private Integer total;
    private Integer okNum;
    private Integer failNum;
    private String status;
    private LocalDateTime createdAt;
    private List<UpItemVo> items;
}
