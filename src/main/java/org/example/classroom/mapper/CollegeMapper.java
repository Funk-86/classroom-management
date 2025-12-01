package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.College;

import java.util.List;

@Mapper
public interface CollegeMapper extends BaseMapper<College> {

    @Select("SELECT c.*, camp.campus_name, u.user_name as dean_name " +
            "FROM colleges c " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.dean_id = u.user_id " +
            "WHERE c.college_id = #{collegeId}")
    College selectCollegeWithDetail(String collegeId);

    @Select("SELECT c.*, camp.campus_name, u.user_name as dean_name " +
            "FROM colleges c " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.dean_id = u.user_id " +
            "WHERE c.campus_id = #{campusId}")
    List<College> selectCollegesByCampus(String campusId);

    @Select("SELECT c.*, camp.campus_name, u.user_name as dean_name " +
            "FROM colleges c " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.dean_id = u.user_id")
    List<College> selectAllCollegesWithDetail();
}