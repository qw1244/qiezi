package com.cms.video.modules.oplog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.cms.video.modules.oplog.entity.OpLog;

@Mapper
public interface OpLogMapper extends BaseMapper<OpLog> {
}
