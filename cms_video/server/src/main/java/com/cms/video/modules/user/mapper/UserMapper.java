package com.cms.video.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.cms.video.modules.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
