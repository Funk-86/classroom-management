package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classroom.entity.Holiday;

@Mapper
public interface HolidayMapper extends BaseMapper<Holiday> {
}

