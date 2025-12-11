package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classroom.entity.Semester;

@Mapper
public interface SemesterMapper extends BaseMapper<Semester> {
}

