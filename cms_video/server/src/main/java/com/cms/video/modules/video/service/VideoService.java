package com.cms.video.modules.video.service;

import com.cms.video.modules.video.dto.VideoSaveReq;
import com.cms.video.modules.video.vo.VideoVo;

public interface VideoService {

    VideoVo play(Long id, Long userId);

    Long save(VideoSaveReq req);

    void remove(Long id);

    void renumber(Long seriesId);
}
