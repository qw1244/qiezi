package com.cms.video.modules.interact.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.interact.dto.WatchReq;
import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.video.vo.VideoVo;

public interface InteractService {

    void like(Long userId, Long videoId);

    void unlike(Long userId, Long videoId);

    void fav(Long userId, Long seriesId);

    void unfav(Long userId, Long seriesId);

    void reportWatch(Long userId, WatchReq req);

    PageVo<SeriesVo> myFav(Long userId, long page, long size);

    
    PageVo<SeriesVo> myHistory(Long userId, long page, long size);

    PageVo<VideoVo> myLikes(Long userId, long page, long size);
}
