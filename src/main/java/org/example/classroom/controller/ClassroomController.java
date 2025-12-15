package org.example.classroom.controller;

import org.example.classroom.dto.ClassroomRealtimeResponse;
import org.example.classroom.dto.ClassroomConflictResult;
import org.example.classroom.dto.ClassroomResponse;
import org.example.classroom.dto.R;
import org.example.classroom.entity.Classroom;
import org.example.classroom.service.ClassroomOccupationService;
import org.example.classroom.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private ClassroomOccupationService classroomOccupationService;

    // 根据教学楼ID获取教室列表
    @GetMapping("/list")
    public R getClassroomsByBuilding(@RequestParam String buildingId) {
        List<Classroom> classrooms = classroomService.getClassroomsByBuilding(buildingId);
        // 实时检测当前占用状态，避免课程时间段显示为空闲
        List<ClassroomResponse> responses = classrooms.stream()
                .map(this::convertToResponseWithRealtimeStatus)
                .collect(Collectors.toList());
        return R.ok().put("data", responses);
    }

    // 根据ID获取教室详情
    @GetMapping("/{id}")
    public R getClassroomById(@PathVariable String id) {
        Classroom classroom = classroomService.getClassroomById(id);
        if (classroom != null) {
            ClassroomResponse response = convertToResponseWithRealtimeStatus(classroom);
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
            ClassroomResponse response = convertToResponseWithRealtimeStatus(classroom);
            return R.ok().put("data", response);
        }
        return R.error("教室不存在");
    }

    /**
     * 获取所有教室的实时状态（按教学楼、楼层排序）
     * 提供给小程序“查看教室”功能使用
     */
    @GetMapping("/status/all")
    public R getAllClassroomsStatus() {
        List<Classroom> classrooms = classroomService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Classroom>()
                        .orderByAsc("building_id")
                        .orderByAsc("floor_num")
                        .orderByAsc("classroom_name")
        );
        List<ClassroomResponse> responses = classrooms.stream()
                .map(this::convertToResponseWithRealtimeStatus)
                .collect(Collectors.toList());
        return R.ok().put("data", responses);
    }

    // 搜索教室名称或编号
    @GetMapping("/search")
    public R searchClassroomsByNameOrId(@RequestParam String keyword) {
        List<Classroom> classrooms = classroomService.searchClassroomsByNameOrId(keyword);
        List<ClassroomResponse> responses = classrooms.stream()
                .map(this::convertToResponseWithRealtimeStatus)
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

    // 扫码实时状态
    @GetMapping("/realtime/{classroomId}")
    public R getClassroomRealtime(@PathVariable String classroomId) {
        try {
            ClassroomRealtimeResponse data = classroomOccupationService.getRealtimeStatus(classroomId);
            return R.ok().put("data", data);
        } catch (Exception e) {
            return R.error("获取教室实时状态失败: " + e.getMessage());
        }
    }

    // 转换方法
    /**
     * 转换并计算实时占用状态
     */
    private ClassroomResponse convertToResponseWithRealtimeStatus(Classroom classroom) {
        ClassroomResponse response = new ClassroomResponse();
        response.setClassroomId(classroom.getClassroomId());
        response.setClassroomName(classroom.getClassroomName());
        response.setBuildingId(classroom.getBuildingId());
        response.setFloorNum(classroom.getFloorNum());
        response.setCapacity(classroom.getCapacity());
        response.setEquipment(classroom.getEquipment());

        // 默认使用数据库状态，作为兜底
        Integer status = classroom.getStatus();
        try {
            // 使用统一的占用检测，判断当前时间段是否被占用（固定东八区，避免服务器时区偏差）
            ZonedDateTime shanghaiNow = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
            LocalDate today = shanghaiNow.toLocalDate();
            LocalTime now = shanghaiNow.toLocalTime();
            ClassroomConflictResult conflict = classroomOccupationService.checkClassroomOccupation(
                    classroom.getClassroomId(),
                    today,
                    now.minusMinutes(5), // 放宽5分钟，避免边界误差/秒级偏移
                    now.plusMinutes(5),
                    null,
                    null
            );
            // 优先以实时占用检测结果为准，避免数据库状态长期不被重置导致“假占用”
            if (conflict != null && conflict.isHasConflict()) {
                status = 1; // 当前时间段存在课程 / 预约 / 签到活动，占用中
            } else {
                // 无任何占用冲突时，一律视为空闲（不再沿用旧的占用状态）
                status = 0;
            }
        } catch (Exception e) {
            // 出现异常时保持数据库状态，避免接口失败
        }
        response.setStatus(status);
        response.setCreatedAt(classroom.getCreatedAt());
        response.setUpdatedAt(classroom.getUpdatedAt());
        return response;
    }
}