package org.example.classroom.controller;

import org.example.classroom.dto.ClassroomResponse;
import org.example.classroom.dto.R;
import org.example.classroom.entity.Classroom;
import org.example.classroom.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    // 根据教学楼ID获取教室列表
    @GetMapping("/list")
    public R getClassroomsByBuilding(@RequestParam String buildingId) {
        List<Classroom> classrooms = classroomService.getClassroomsByBuilding(buildingId);
        List<ClassroomResponse> responses = classrooms.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return R.ok().put("data", responses);
    }

    // 根据ID获取教室详情
    @GetMapping("/{id}")
    public R getClassroomById(@PathVariable String id) {
        Classroom classroom = classroomService.getClassroomById(id);
        if (classroom != null) {
            ClassroomResponse response = convertToResponse(classroom);
            return R.ok().put("data", response);
        } else {
            return R.error("教室不存在");
        }
    }

    // 获取教室状态
    @GetMapping("/status/{classroomId}")
    public R getClassroomStatus(@PathVariable String classroomId) {
        Classroom classroom = classroomService.getClassroomById(classroomId);
        if (classroom != null) {
            ClassroomResponse response = convertToResponse(classroom);
            return R.ok().put("data", response);
        }
        return R.error("教室不存在");
    }

    // 搜索教室名称或编号
    @GetMapping("/search")
    public R searchClassroomsByNameOrId(@RequestParam String keyword) {
        List<Classroom> classrooms = classroomService.searchClassroomsByNameOrId(keyword);
        List<ClassroomResponse> responses = classrooms.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return R.ok().put("data", responses);
    }

    // 添加教室
    @PostMapping("/add")
    public R addClassroom(@RequestBody Classroom classroom) {
        // 参数校验
        if (classroom.getClassroomId() == null || classroom.getClassroomId().trim().isEmpty()) {
            return R.error("教室ID不能为空");
        }
        if (classroom.getClassroomName() == null || classroom.getClassroomName().trim().isEmpty()) {
            return R.error("教室名称不能为空");
        }
        if (classroom.getBuildingId() == null || classroom.getBuildingId().trim().isEmpty()) {
            return R.error("教学楼ID不能为空");
        }

        boolean success = classroomService.addClassroom(classroom);
        if (success) {
            return R.ok("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    // 更新教室信息
    @PutMapping("/update")
    public R updateClassroom(@RequestBody Classroom classroom) {
        if (classroom.getClassroomId() == null || classroom.getClassroomId().trim().isEmpty()) {
            return R.error("教室ID不能为空");
        }

        boolean success = classroomService.updateClassroom(classroom);
        if (success) {
            return R.ok("更新成功");
        } else {
            return R.error("更新失败");
        }
    }

    // 删除教室
    @DeleteMapping("/delete/{classroomId}")
    public R deleteClassroom(@PathVariable String classroomId) {
        boolean success = classroomService.deleteClassroom(classroomId);
        if (success) {
            return R.ok("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    // 转换方法
    private ClassroomResponse convertToResponse(Classroom classroom) {
        ClassroomResponse response = new ClassroomResponse();
        response.setClassroomId(classroom.getClassroomId());
        response.setClassroomName(classroom.getClassroomName());
        response.setBuildingId(classroom.getBuildingId());
        response.setFloorNum(classroom.getFloorNum());
        response.setCapacity(classroom.getCapacity());
        response.setEquipment(classroom.getEquipment());
        response.setStatus(classroom.getStatus());
        response.setCreatedAt(classroom.getCreatedAt());
        response.setUpdatedAt(classroom.getUpdatedAt());
        return response;
    }
}