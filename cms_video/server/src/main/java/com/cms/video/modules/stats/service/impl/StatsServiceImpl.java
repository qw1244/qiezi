package com.cms.video.modules.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.video.modules.interact.entity.Fav;
import com.cms.video.modules.interact.entity.VLike;
import com.cms.video.modules.interact.entity.WatchLog;
import com.cms.video.modules.interact.mapper.FavMapper;
import com.cms.video.modules.interact.mapper.VLikeMapper;
import com.cms.video.modules.interact.mapper.WatchLogMapper;
import com.cms.video.modules.series.entity.Series;
import com.cms.video.modules.series.mapper.SeriesMapper;
import com.cms.video.modules.stats.service.StatsService;
import com.cms.video.modules.stats.vo.DashNameValue;
import com.cms.video.modules.stats.vo.DashVo;
import com.cms.video.modules.user.entity.User;
import com.cms.video.modules.user.mapper.UserMapper;
import com.cms.video.modules.video.entity.Video;
import com.cms.video.modules.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    private final UserMapper userMapper;
    private final SeriesMapper seriesMapper;
    private final VideoMapper videoMapper;
    private final VLikeMapper vLikeMapper;
    private final FavMapper favMapper;
    private final WatchLogMapper watchLogMapper;

    @Override
    public DashVo dash() {
        DashVo vo = new DashVo();
        vo.setUserCount(n(userMapper.selectCount(new LambdaQueryWrapper<User>())));
        vo.setSeriesCount(n(seriesMapper.selectCount(new LambdaQueryWrapper<Series>())));
        vo.setVideoCount(n(videoMapper.selectCount(new LambdaQueryWrapper<Video>())));
        vo.setLikeSum(n(vLikeMapper.selectCount(new LambdaQueryWrapper<VLike>())));
        vo.setFavSum(n(favMapper.selectCount(new LambdaQueryWrapper<Fav>())));
        vo.setWatchCount(n(watchLogMapper.selectCount(new LambdaQueryWrapper<WatchLog>())));
        long play = 0;
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>().select(Video::getPlayCount));
        for (Video v : videos) {
            play += v.getPlayCount() == null ? 0 : v.getPlayCount();
        }
        vo.setPlaySum(play);

        vo.setWatchTrend(buildWatchTrend());
        vo.setTopSeries(buildTopSeries());
        List<DashNameValue> mix = new ArrayList<>();
        mix.add(new DashNameValue("播放", play));
        mix.add(new DashNameValue("点赞", vo.getLikeSum()));
        mix.add(new DashNameValue("收藏", vo.getFavSum()));
        mix.add(new DashNameValue("足迹", vo.getWatchCount()));
        vo.setInteractMix(mix);
        return vo;
    }

    private List<DashNameValue> buildWatchTrend() {
        List<DashNameValue> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            long count = n(watchLogMapper.selectCount(new LambdaQueryWrapper<WatchLog>()
                    .ge(WatchLog::getUpdatedAt, start)
                    .lt(WatchLog::getUpdatedAt, end)));
            list.add(new DashNameValue(day.format(DAY_LABEL), count));
        }
        return list;
    }

    private List<DashNameValue> buildTopSeries() {
        List<Series> series = seriesMapper.selectList(new LambdaQueryWrapper<Series>()
                .orderByDesc(Series::getPlayCount)
                .last("limit 5"));
        List<DashNameValue> list = new ArrayList<>();
        for (Series s : series) {
            String title = StringUtils.hasText(s.getTitle()) ? s.getTitle() : ("合集#" + s.getId());
            if (title.length() > 12) {
                title = title.substring(0, 12) + "…";
            }
            list.add(new DashNameValue(title, s.getPlayCount() == null ? 0 : s.getPlayCount()));
        }
        return list;
    }

    private long n(Long v) {
        return v == null ? 0 : v;
    }
}
