package org.example.classroom.service.impl;

import org.example.classroom.entity.Building;
import org.example.classroom.mapper.BuildingMapper;
import org.example.classroom.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public List<Building> getBuildingsByCampus(String campusId) {
        return buildingMapper.selectByCampusId(campusId);
    }

    @Override
    public List<Building> getAllBuildings() {
        return buildingMapper.selectAllBuildings();
    }

    @Override
    public boolean addBuilding(Building building) {
        return buildingMapper.insertBuilding(building) > 0;
    }

    @Override
    public boolean updateBuilding(Building building) {
        return buildingMapper.updateBuilding(building) > 0;
    }

    @Override
    public boolean deleteBuilding(String buildingId) {
        return buildingMapper.deleteBuilding(buildingId) > 0;
    }

    @Override
    public Building getBuildingById(String buildingId) {
        return buildingMapper.selectBuildingById(buildingId);
    }
}