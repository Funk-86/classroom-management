package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.Campus;
import org.example.classroom.mapper.CampusMapper;
import org.example.classroom.service.CampusService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusService {

    @Override
    public List<Campus> getAllCampuses() {
        return baseMapper.selectAllCampuses();
    }

    @Override
    public Campus getNearestCampus(double latitude, double longitude) {
        return baseMapper.findNearestCampus(latitude, longitude);
    }

    @Override
    public List<Campus> getCampusesByDistrict(String district) {
        return baseMapper.selectByDistrict(district);
    }
}