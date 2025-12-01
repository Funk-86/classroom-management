package org.example.classroom.controller;

import org.example.classroom.dto.ClassroomConflictResult;
import org.example.classroom.dto.ClassroomOccupationCheckRequest;
import org.example.classroom.dto.ClassroomOccupationInfo;
import org.example.classroom.dto.R;
import org.example.classroom.service.ClassroomOccupationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 教室占用冲突检测控制器
 */
@RestController
@RequestMapping("/api/classroom-occupation")
public class ClassroomOccupationController {

    @Autowired
    private ClassroomOccupationService occupationService;

    /**
     * 检查教室占用冲突（统一检测）
     */
    @PostMapping("/check-conflict")
    public R checkConflict(@Valid @RequestBody ClassroomOccupationCheckRequest request) {
        try {
            // 验证时间范围
            String validationError = request.validate();
            if (validationError != null) {
                return R.error(400, validationError);
            }

            ClassroomConflictResult result = occupationService.checkClassroomOccupation(
                    request.getClassroomId(),
                    request.getDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getExcludeReservationId(),
                    request.getExcludeScheduleId()
            );

            return R.ok()
                    .put("hasConflict", result.isHasConflict())
                    .put("conflictType", result.getConflictType())
                    .put("conflicts", result.getConflicts())
                    .put("message", result.getMessage());
        } catch (Exception e) {
            return R.error("检查教室占用失败: " + e.getMessage());
        }
    }

    /**
     * 获取教室在指定日期的所有占用信息
     */
    @GetMapping("/occupations")
    public R getClassroomOccupations(
            @RequestParam String classroomId,
            @RequestParam LocalDate date
    ) {
        try {
            List<ClassroomOccupationInfo> occupations = occupationService.getClassroomOccupations(
                    classroomId, date
            );
            return R.ok().put("data", occupations);
        } catch (Exception e) {
            return R.error("获取教室占用信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取教室在指定时间段内的占用信息
     */
    @GetMapping("/occupations-in-range")
    public R getClassroomOccupationsInTimeRange(
            @RequestParam String classroomId,
            @RequestParam LocalDate date,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        try {
            java.time.LocalTime start = java.time.LocalTime.parse(startTime);
            java.time.LocalTime end = java.time.LocalTime.parse(endTime);

            List<ClassroomOccupationInfo> occupations = occupationService.getClassroomOccupationsInTimeRange(
                    classroomId, date, start, end
            );
            return R.ok().put("data", occupations);
        } catch (Exception e) {
            return R.error("获取教室占用信息失败: " + e.getMessage());
        }
    }
}

