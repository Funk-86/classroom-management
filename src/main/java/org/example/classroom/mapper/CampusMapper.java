package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.classroom.entity.Campus;
import java.util.List;

@Mapper
public interface CampusMapper extends BaseMapper<Campus> {

    @Select("SELECT * FROM campuses")
    List<Campus> selectAllCampuses();

    @Select("SELECT * FROM campuses WHERE district = #{district}")
    List<Campus> selectByDistrict(String district);

    @Select("SELECT *, " +
            "(6371 * acos(cos(radians(#{latitude})) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(#{longitude})) + " +
            "sin(radians(#{latitude})) * sin(radians(latitude)))) AS distance " +
            "FROM campuses ORDER BY distance ASC LIMIT 1")
    @Results({
            @Result(property = "campusId", column = "campus_id"),
            @Result(property = "campusName", column = "campus_name"),
            @Result(property = "district", column = "district"),
            @Result(property = "address", column = "address"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "distance", column = "distance")
    })
    Campus findNearestCampus(double latitude, double longitude);
}