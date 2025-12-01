package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.College;
import org.example.classroom.mapper.CollegeMapper;
import org.example.classroom.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    @Autowired
    private CollegeMapper collegeMapper;

    @Override
    public College getCollegeWithDetail(String collegeId) {
        return collegeMapper.selectCollegeWithDetail(collegeId);
    }

    @Override
    public List<College> getCollegesByCampus(String campusId) {
        return collegeMapper.selectCollegesByCampus(campusId);
    }

    @Override
    public List<College> getAllCollegesWithDetail() {
        return collegeMapper.selectAllCollegesWithDetail();
    }


    @Override
    public boolean createCollege(College college) {
        return save(college);
    }

    @Override
    public boolean updateCollegeInfo(College college) {
        return updateById(college);
    }
}