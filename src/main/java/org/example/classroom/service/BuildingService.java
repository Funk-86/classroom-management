package org.example.classroom.service;

import org.example.classroom.entity.Building;
import java.util.List;

public interface BuildingService {
    List<Building> getBuildingsByCampus(String campusId);
    List<Building> getAllBuildings();
    boolean addBuilding(Building building);
    boolean updateBuilding(Building building);
    boolean deleteBuilding(String buildingId);
    Building getBuildingById(String buildingId);
}