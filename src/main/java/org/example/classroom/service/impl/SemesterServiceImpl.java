package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.Semester;
import org.example.classroom.mapper.SemesterMapper;
import org.example.classroom.service.SemesterService;
import org.springframework.stereotype.Service;

@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {
}

