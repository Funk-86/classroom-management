package org.example.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.College;

import java.util.List;

public interface CollegeService extends IService<College> {

    College getCollegeWithDetail(String collegeId);

    List<College> getCollegesByCampus(String campusId);

    List<College> getAllCollegesWithDetail();

    boolean createCollege(College college);

    boolean updateCollegeInfo(College college);
}


