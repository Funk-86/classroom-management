package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.College;
import org.example.classroom.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/college")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @GetMapping("/list")
    public R getAllColleges() {
        try {
            List<College> colleges = collegeService.getAllCollegesWithDetail();
            return R.ok().put("data", colleges);
        } catch (Exception e) {
            return R.error("获取学院列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{collegeId}")
    public R getCollegeDetail(@PathVariable String collegeId) {
        try {
            College college = collegeService.getCollegeWithDetail(collegeId);
            if (college != null) {
                return R.ok().put("data", college);
            } else {
                return R.error("学院不存在");
            }
        } catch (Exception e) {
            return R.error("获取学院详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/campus/{campusId}")
    public R getCollegesByCampus(@PathVariable String campusId) {
        try {
            List<College> colleges = collegeService.getCollegesByCampus(campusId);
            return R.ok().put("data", colleges);
        } catch (Exception e) {
            return R.error("获取校区学院列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public R createCollege(@RequestBody College college) {
        try {
            if (college.getCollegeName() == null || college.getCollegeName().trim().isEmpty()) {
                return R.error("学院名称不能为空");
            }
            if (college.getCampusId() == null || college.getCampusId().trim().isEmpty()) {
                return R.error("所属校区不能为空");
            }

            boolean success = collegeService.createCollege(college);
            if (success) {
                return R.ok("创建学院成功");
            } else {
                return R.error("创建学院失败");
            }
        } catch (Exception e) {
            return R.error("创建学院失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public R updateCollege(@RequestBody College college) {
        try {
            if (college.getCollegeId() == null || college.getCollegeId().trim().isEmpty()) {
                return R.error("学院ID不能为空");
            }

            boolean success = collegeService.updateCollegeInfo(college);
            if (success) {
                return R.ok("更新学院信息成功");
            } else {
                return R.error("更新学院信息失败");
            }
        } catch (Exception e) {
            return R.error("更新学院信息失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{collegeId}")
    public R deleteCollege(@PathVariable String collegeId) {
        try {
            boolean success = collegeService.removeById(collegeId);
            if (success) {
                return R.ok("删除学院成功");
            } else {
                return R.error("删除学院失败");
            }
        } catch (Exception e) {
            return R.error("删除学院失败: " + e.getMessage());
        }
    }
}