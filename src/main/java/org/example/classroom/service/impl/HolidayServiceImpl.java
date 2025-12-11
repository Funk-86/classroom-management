package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.Holiday;
import org.example.classroom.mapper.HolidayMapper;
import org.example.classroom.service.HolidayService;
import org.springframework.stereotype.Service;

@Service
public class HolidayServiceImpl extends ServiceImpl<HolidayMapper, Holiday> implements HolidayService {
}

