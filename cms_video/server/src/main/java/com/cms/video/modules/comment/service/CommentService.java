package com.cms.video.modules.comment.service;

import com.cms.video.common.PageVo;
import com.cms.video.modules.comment.dto.CommentPostReq;
import com.cms.video.modules.comment.vo.CommentVo;

public interface CommentService {

    PageVo<CommentVo> pageByVideo(Long videoId, long page, long size, Long userId);

    CommentVo post(Long userId, CommentPostReq req);

    void delete(Long userId, Long id);

    PageVo<CommentVo> pageAdmin(long page, long size);

    void deleteByAdmin(Long id);
}
