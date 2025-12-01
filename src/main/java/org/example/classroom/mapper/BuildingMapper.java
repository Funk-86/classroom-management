package org.example.classroom.mapper;

import org.apache.ibatis.annotations.*;
import org.example.classroom.entity.Building;
import java.util.List;

@Mapper
public interface BuildingMapper {

    @Select("SELECT * FROM buildings WHERE campus_id = #{campusId}")
    List<Building> selectByCampusId(String campusId);

    @Select("SELECT * FROM buildings")
    List<Building> selectAllBuildings();

    @Select("SELECT * FROM buildings WHERE building_id = #{buildingId}")
    Building selectBuildingById(String buildingId);

    @Insert("INSERT INTO buildings (building_id, building_name, location, floors, campus_id) " +
            "VALUES (#{buildingId}, #{buildingName}, #{location}, #{floors}, #{campusId})")
    int insertBuilding(Building building);

    @Update("UPDATE buildings SET building_name = #{buildingName}, location = #{location}, " +
            "floors = #{floors}, campus_id = #{campusId} WHERE building_id = #{buildingId}")
    int updateBuilding(Building building);

    @Delete("DELETE FROM buildings WHERE building_id = #{buildingId}")
    int deleteBuilding(String buildingId);
}