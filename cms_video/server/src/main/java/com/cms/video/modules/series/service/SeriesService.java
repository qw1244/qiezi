package com.cms.video.modules.series.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.series.dto.SeriesSaveReq;
import com.cms.video.modules.series.vo.SeriesDetailVo;
import com.cms.video.modules.series.vo.SeriesVo;

public interface SeriesService {

    PageVo<SeriesVo> pageFront(String keyword, long page, long size, Long userId);

    SeriesDetailVo detail(Long id, Long userId);

    PageVo<SeriesVo> pageAdmin(String keyword, Integer status, long page, long size);

    SeriesDetailVo adminDetail(Long id);

    Long save(SeriesSaveReq req);

    void remove(Long id);
}
