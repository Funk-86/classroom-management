package org.example.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.Campus;
import java.util.List;

public interface CampusService extends IService<Campus> {
    List<Campus> getAllCampuses();
    Campus getNearestCampus(double latitude, double longitude);
    List<Campus> getCampusesByDistrict(String district);
}