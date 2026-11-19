package com.cms.video.modules.admin.vo;

import com.cms.video.modules.series.vo.SeriesVo;
import com.cms.video.modules.user.vo.UserVo;
import com.cms.video.modules.video.vo.VideoVo;
import lombok.Data;

import java.util.List;

@Data
public class UserDetailVo {

    private UserVo user;
    private long likeCount;
    private long favCount;
    private long watchCount;
    private List<VideoVo> likes;
    private List<SeriesVo> favs;
    private List<SeriesVo> history;
}
