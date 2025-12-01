package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.Campus;
import org.example.classroom.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campus")
public class CampusController {

    @Autowired
    private CampusService campusService;

    // 获取最近校区
    @GetMapping("/nearest")
    public R getNearestCampus(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        // 参数校验
        if (latitude < -90 || latitude > 90) {
            return R.error("纬度必须在-90到90度之间");
        }
        if (longitude < -180 || longitude > 180) {
            return R.error("经度必须在-180到180度之间");
        }

        Campus campus = campusService.getNearestCampus(latitude, longitude);
        if (campus != null) {
            return R.ok().put("data", campus);
        }
        return R.error("未找到最近校区");
    }

    // 获取所有校区
    @GetMapping("/list")
    public R getAllCampuses() {
        return R.ok().put("data", campusService.getAllCampuses());
    }

    // 根据区域获取校区
    @GetMapping("/district")
    public R getCampusesByDistrict(@RequestParam String district) {
        return R.ok().put("data", campusService.getCampusesByDistrict(district));
    }
}