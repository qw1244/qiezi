package com.cms.video.modules.series.vo;

import com.cms.video.modules.video.vo.VideoVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeriesDetailVo extends SeriesVo {
    private List<VideoVo> videos;
    private Long resumeVideoId;
    private Integer resumeProgress;
}
