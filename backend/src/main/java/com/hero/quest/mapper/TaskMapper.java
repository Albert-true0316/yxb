package com.hero.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.quest.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
