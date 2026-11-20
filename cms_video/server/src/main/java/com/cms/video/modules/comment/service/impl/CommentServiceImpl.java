package com.cms.video.modules.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.video.common.BizException;
import com.cms.video.common.PageVo;
import com.cms.video.modules.comment.dto.CommentPostReq;
import com.cms.video.modules.comment.entity.Comment;
import com.cms.video.modules.comment.mapper.CommentMapper;
import com.cms.video.modules.comment.service.CommentService;
import com.cms.video.modules.comment.vo.CommentVo;
import com.cms.video.modules.user.entity.User;
import com.cms.video.modules.user.mapper.UserMapper;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;

    @Override
    public PageVo<CommentVo> pageByVideo(Long videoId, long page, long size, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || (video.getStatus() != null && video.getStatus() != 1)) {
            throw new BizException("视频不存在");
        }
        Page<Comment> p = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getVideoId, videoId)
                        .orderByDesc(Comment::getCreatedAt)
                        .orderByDesc(Comment::getId));
        return PageVo.of(p.getTotal(), toVos(p.getRecords(), userId, false));
    }

    @Override
    @Transactional
    public CommentVo post(Long userId, CommentPostReq req) {
        Video video = videoMapper.selectById(req.getVideoId());
        if (video == null || (video.getStatus() != null && video.getStatus() != 1)) {
            throw new BizException("视频不存在");
        }
        String content = req.getContent() == null ? "" : req.getContent().trim();
        if (!StringUtils.hasText(content)) {
            throw new BizException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new BizException("评论最多500字");
        }
        Comment comment = new Comment();
        comment.setVideoId(req.getVideoId());
        comment.setUserId(userId);
        comment.setContent(content);
        commentMapper.insert(comment);
        List<CommentVo> vos = toVos(Collections.singletonList(comment), userId, false);
        return vos.get(0);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            throw new BizException("只能删除自己的评论");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public PageVo<CommentVo> pageAdmin(long page, long size) {
        Page<Comment> p = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .orderByDesc(Comment::getCreatedAt)
                        .orderByDesc(Comment::getId));
        return PageVo.of(p.getTotal(), toVos(p.getRecords(), null, true));
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        commentMapper.deleteById(id);
    }

    /** 批量补昵称/头像；管理端再补视频标题。 */
    private List<CommentVo> toVos(List<Comment> comments, Long currentUserId, boolean withVideoTitle) {
        List<CommentVo> list = new ArrayList<>();
        if (comments.isEmpty()) {
            return list;
        }
        Map<Long, User> users = new HashMap<>();
        List<Long> userIds = comments.stream().map(Comment::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> users.put(u.getId(), u));
        }
        Map<Long, String> videoTitles = new HashMap<>();
        if (withVideoTitle) {
            List<Long> videoIds = comments.stream().map(Comment::getVideoId)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!videoIds.isEmpty()) {
                videoMapper.selectBatchIds(videoIds).forEach(v -> videoTitles.put(v.getId(), v.getTitle()));
            }
        }
        for (Comment c : comments) {
            CommentVo vo = new CommentVo();
            vo.setId(c.getId());
            vo.setVideoId(c.getVideoId());
            vo.setUserId(c.getUserId());
            User u = users.get(c.getUserId());
            vo.setNickname(u != null && StringUtils.hasText(u.getNickname()) ? u.getNickname() : "匿名用户");
            vo.setAvatar(u != null ? u.getAvatar() : null);
            vo.setVideoTitle(videoTitles.get(c.getVideoId()));
            vo.setContent(c.getContent());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setMine(currentUserId != null && currentUserId.equals(c.getUserId()));
            list.add(vo);
        }
        return list;
    }
}
