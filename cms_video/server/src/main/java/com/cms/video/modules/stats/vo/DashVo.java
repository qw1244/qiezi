package com.cms.video.modules.stats.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashVo {

    private long userCount;
    private long seriesCount;
    private long videoCount;
    private long playSum;
    private long likeSum;
    private long favSum;
    private long watchCount;

    
    private List<DashNameValue> watchTrend = new ArrayList<>();

    
    private List<DashNameValue> topSeries = new ArrayList<>();

    
    private List<DashNameValue> interactMix = new ArrayList<>();
}
