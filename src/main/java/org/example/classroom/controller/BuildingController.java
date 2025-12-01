package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.Building;
import org.example.classroom.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    // 根据校区获取教学楼
    @GetMapping("/list")
    public R getBuildingsByCampus(@RequestParam String campusId) {
        List<Building> buildings = buildingService.getBuildingsByCampus(campusId);
        return R.ok().put("data", buildings);
    }

    // 获取所有教学楼
    @GetMapping("/all")
    public R getAllBuildings() {
        List<Building> buildings = buildingService.getAllBuildings();
        return R.ok().put("data", buildings);
    }

    // 根据ID获取教学楼详情
    @GetMapping("/{buildingId}")
    public R getBuildingById(@PathVariable String buildingId) {
        Building building = buildingService.getBuildingById(buildingId);
        if (building != null) {
            return R.ok().put("data", building);
        } else {
            return R.error("教学楼不存在");
        }
    }

    // 添加教学楼
    @PostMapping("/add")
    public R addBuilding(@RequestBody Building building) {
        // 参数校验
        if (building.getBuildingId() == null || building.getBuildingId().trim().isEmpty()) {
            return R.error("教学楼ID不能为空");
        }
        if (building.getBuildingName() == null || building.getBuildingName().trim().isEmpty()) {
            return R.error("教学楼名称不能为空");
        }

        boolean success = buildingService.addBuilding(building);
        if (success) {
            return R.ok("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    // 更新教学楼信息
    @PutMapping("/update")
    public R updateBuilding(@RequestBody Building building) {
        if (building.getBuildingId() == null || building.getBuildingId().trim().isEmpty()) {
            return R.error("教学楼ID不能为空");
        }

        boolean success = buildingService.updateBuilding(building);
        if (success) {
            return R.ok("更新成功");
        } else {
            return R.error("更新失败");
        }
    }

    // 删除教学楼
    @DeleteMapping("/delete/{buildingId}")
    public R deleteBuilding(@PathVariable String buildingId) {
        boolean success = buildingService.deleteBuilding(buildingId);
        if (success) {
            return R.ok("删除成功");
        } else {
            return R.error("删除失败");
        }
    }
}