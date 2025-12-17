package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.CourseClass;

import java.util.List;

@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {

    // 根据课程ID获取所有关联的班级
    @Select("SELECT cc.*, c.class_name, c.class_code " +
            "FROM course_classes cc " +
            "LEFT JOIN classes c ON cc.class_id = c.class_id " +
            "WHERE cc.course_id = #{courseId}")
    List<CourseClass> selectByCourseId(@Param("courseId") String courseId);

    // 根据班级ID获取所有关联的课程
    @Select("SELECT cc.* FROM course_classes cc WHERE cc.class_id = #{classId}")
    List<CourseClass> selectByClassId(@Param("classId") String classId);

    // 删除课程的所有班级关联
    @Delete("DELETE FROM course_classes WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") String courseId);

    // 删除班级的所有课程关联
    @Delete("DELETE FROM course_classes WHERE class_id = #{classId}")
    int deleteByClassId(@Param("classId") String classId);

    // 检查关联是否存在
    @Select("SELECT COUNT(*) FROM course_classes WHERE course_id = #{courseId} AND class_id = #{classId}")
    int checkExists(@Param("courseId") String courseId, @Param("classId") String classId);
}

