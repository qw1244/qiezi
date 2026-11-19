package com.cms.video.modules.series.vo;

import lombok.Data;

@Data
public class SeriesVo {
    private Long id;
    private String title;
    private String cover;
    private String intro;
    private Integer status;
    private Integer epCount;
    private Long playCount;
    private Long favCount;
    private Boolean fav;
    private Integer firstProgress;
    
    private Integer lastEpNo;
    
    private Integer lastProgress;
    
    private Long lastVideoId;
}
