package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.CourseSchedule;
import org.example.classroom.service.CourseService;
import org.example.classroom.util.WeekCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    @Autowired
    private CourseService courseService;

    // 获取学生个人课表
    @GetMapping("/student")
    public R getStudentTimetable(@RequestParam String studentId,
                                 @RequestParam(required = false) String date,
                                 @RequestParam(required = false) String week) {
        try {
            LocalDate queryDate;
            if (date != null) {
                queryDate = LocalDate.parse(date);
            } else if (week != null) {
                int weekNum = Integer.parseInt(week);
                WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNum);
                queryDate = weekRange.getStartDate();
            } else {
                queryDate = LocalDate.now();
            }

            List<CourseSchedule> timetable = courseService.getStudentTimetable(studentId, queryDate);
            return R.ok().put("data", timetable);
        } catch (Exception e) {
            return R.error("获取学生课表失败: " + e.getMessage());
        }
    }

    // 获取教师个人课表
    @GetMapping("/teacher")
    public R getTeacherTimetable(@RequestParam String teacherId,
                                 @RequestParam(required = false) String date,
                                 @RequestParam(required = false) String academicYear,
                                 @RequestParam(required = false) Integer semester) {
        try {
            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            List<CourseSchedule> timetable = courseService.getTeacherTimetable(teacherId, queryDate);
            return R.ok().put("data", timetable);
        } catch (Exception e) {
            return R.error("获取教师课表失败: " + e.getMessage());
        }
    }

    // 获取班级课表（按班级或专业）
    @GetMapping("/class")
    public R getClassTimetable(@RequestParam String classId,
                               @RequestParam(required = false) String date,
                               @RequestParam(required = false) String academicYear,
                               @RequestParam(required = false) Integer semester) {
        try {
            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            int weekNumber = courseService.getWeekNumber(queryDate);

            List<CourseSchedule> timetable = courseService.getClassTimetableByWeek(classId, weekNumber);

            // 按天分组
            Map<Integer, List<CourseSchedule>> groupedByDay = timetable.stream()
                    .collect(Collectors.groupingBy(schedule ->
                            schedule.getDayOfWeek() != null ?
                                    schedule.getDayOfWeek() :
                                    schedule.getScheduleDate().getDayOfWeek().getValue()
                    ));

            return R.ok().put("data", groupedByDay)
                    .put("classId", classId)
                    .put("weekNumber", weekNumber);
        } catch (Exception e) {
            return R.error("获取班级课表失败: " + e.getMessage());
        }
    }

    // 获取教室课表（查看某个教室的课程安排）
    @GetMapping("/classroom")
    public R getClassroomTimetable(@RequestParam String classroomId,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(required = false) String week) {
        try {
            LocalDate queryDate;
            int weekNumber;

            if (date != null) {
                queryDate = LocalDate.parse(date);
                weekNumber = courseService.getWeekNumber(queryDate);
            } else if (week != null) {
                weekNumber = Integer.parseInt(week);
                WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);
                queryDate = weekRange.getStartDate();
            } else {
                weekNumber = courseService.getCurrentWeek();
                WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);
                queryDate = weekRange.getStartDate();
            }

            List<CourseSchedule> timetable = courseService.getClassroomTimetableByWeek(classroomId, weekNumber);

            // 按天分组
            Map<Integer, List<CourseSchedule>> groupedByDay = timetable.stream()
                    .collect(Collectors.groupingBy(schedule ->
                            schedule.getDayOfWeek() != null ?
                                    schedule.getDayOfWeek() :
                                    schedule.getScheduleDate().getDayOfWeek().getValue()
                    ));

            return R.ok().put("data", groupedByDay)
                    .put("classroomId", classroomId)
                    .put("weekNumber", weekNumber);
        } catch (Exception e) {
            return R.error("获取教室课表失败: " + e.getMessage());
        }
    }

    // 获取教学楼课表
    @GetMapping("/building")
    public R getBuildingTimetable(@RequestParam String buildingId,
                                  @RequestParam(required = false) String date,
                                  @RequestParam(required = false) Integer floor) {
        try {
            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            int weekNumber = courseService.getWeekNumber(queryDate);

            // 获取教学楼内所有教室的课表
            List<CourseSchedule> buildingTimetable = courseService.getSchedulesByWeek(
                    null, null, buildingId, weekNumber);

            // 按教室分组
            Map<String, List<CourseSchedule>> groupedByClassroom = buildingTimetable.stream()
                    .collect(Collectors.groupingBy(CourseSchedule::getClassroomId));

            // 如果指定了楼层，过滤对应楼层的教室
            if (floor != null) {
                // 这里需要根据实际数据库结构过滤对应楼层的教室
                // 简化实现，实际需要关联classrooms表查询楼层信息
            }

            return R.ok().put("data", groupedByClassroom)
                    .put("buildingId", buildingId)
                    .put("weekNumber", weekNumber);
        } catch (Exception e) {
            return R.error("获取教学楼课表失败: " + e.getMessage());
        }
    }

    // 获取今日课程
    @GetMapping("/today")
    public R getTodayTimetable(@RequestParam(required = false) String userId,
                               @RequestParam(required = false) String userRole) {
        try {
            LocalDate today = LocalDate.now();
            List<CourseSchedule> todayCourses;

            if ("teacher".equals(userRole) && userId != null) {
                todayCourses = courseService.getTeacherTimetable(userId, today);
            } else if ("student".equals(userRole) && userId != null) {
                todayCourses = courseService.getStudentTimetable(userId, today);
            } else {
                // 返回所有今日课程
                todayCourses = courseService.getSchedulesWithDetail(
                        null, null, null, null, null, today, today, 1, 1000).getRecords();
            }

            return R.ok().put("data", todayCourses)
                    .put("date", today.toString());
        } catch (Exception e) {
            return R.error("获取今日课程失败: " + e.getMessage());
        }
    }

    // 获取本周课程
    @GetMapping("/week")
    public R getWeekTimetable(@RequestParam(required = false) String userId,
                              @RequestParam(required = false) String userRole) {
        try {
            int currentWeek = courseService.getCurrentWeek();
            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(currentWeek);

            List<CourseSchedule> weekCourses;

            if ("teacher".equals(userRole) && userId != null) {
                weekCourses = courseService.getTeacherTimetableByWeek(userId, currentWeek);
            } else if ("student".equals(userRole) && userId != null) {
                weekCourses = courseService.getStudentTimetableByWeek(userId, currentWeek);
            } else {
                weekCourses = courseService.getSchedulesByWeek(null, null, null, currentWeek);
            }

            // 按天分组
            Map<Integer, List<CourseSchedule>> groupedByDay = weekCourses.stream()
                    .collect(Collectors.groupingBy(schedule ->
                            schedule.getDayOfWeek() != null ?
                                    schedule.getDayOfWeek() :
                                    schedule.getScheduleDate().getDayOfWeek().getValue()
                    ));

            return R.ok().put("data", groupedByDay)
                    .put("weekNumber", currentWeek)
                    .put("startDate", weekRange.getStartDate())
                    .put("endDate", weekRange.getEndDate());
        } catch (Exception e) {
            return R.error("获取本周课程失败: " + e.getMessage());
        }
    }

    // 课程冲突检测
    @PostMapping("/check-conflict")
    public R checkTimetableConflict(@RequestBody CourseSchedule schedule) {
        try {
            boolean hasConflict = courseService.checkScheduleConflict(schedule);
            Map<String, Object> result = Map.of(
                    "hasConflict", hasConflict,
                    "conflictType", hasConflict ? "TIME_CONFLICT" : "NO_CONFLICT",
                    "message", hasConflict ? "存在时间冲突" : "无时间冲突"
            );
            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("检测课程冲突失败: " + e.getMessage());
        }
    }

    // 导出课表（Excel格式）
    @GetMapping("/export")
    public R exportTimetable(@RequestParam String userId,
                             @RequestParam String userRole,
                             @RequestParam(required = false) String format,
                             @RequestParam(required = false) String academicYear,
                             @RequestParam(required = false) Integer semester) {
        try {
            // 获取课表数据
            List<CourseSchedule> timetable;
            String fileName;

            if ("teacher".equals(userRole)) {
                int currentWeek = courseService.getCurrentWeek();
                timetable = courseService.getTeacherTimetableByWeek(userId, currentWeek);
                fileName = "teacher_timetable_export.xlsx";
            } else if ("student".equals(userRole)) {
                int currentWeek = courseService.getCurrentWeek();
                timetable = courseService.getStudentTimetableByWeek(userId, currentWeek);
                fileName = "student_timetable_export.xlsx";
            } else {
                int currentWeek = courseService.getCurrentWeek();
                timetable = courseService.getSchedulesByWeek(null, null, null, currentWeek);
                fileName = "all_timetable_export.xlsx";
            }

            // 这里应该调用Excel导出工具类
            // 简化实现，返回导出信息
            Map<String, Object> exportInfo = Map.of(
                    "fileName", fileName,
                    "downloadUrl", "/api/download/" + fileName,
                    "totalRecords", timetable.size(),
                    "exportTime", LocalDate.now().toString(),
                    "message", "导出功能待具体实现"
            );
            return R.ok().put("data", exportInfo);
        } catch (Exception e) {
            return R.error("导出课表失败: " + e.getMessage());
        }
    }

    // 获取可用的课程时间段
    @GetMapping("/available-slots")
    public R getAvailableTimeSlots(@RequestParam String classroomId,
                                   @RequestParam String date,
                                   @RequestParam(required = false) Integer duration) {
        try {
            LocalDate queryDate = LocalDate.parse(date);
            int slotDuration = duration != null ? duration : 60; // 默认60分钟

            // 生成标准时间段（8:00-21:00）
            List<Map<String, String>> timeSlots = generateTimeSlots();

            // 获取该教室当天的课程安排
            List<CourseSchedule> daySchedules = courseService.getSchedulesWithDetail(
                    null, classroomId, null, null, null, queryDate, queryDate, 1, 100).getRecords();

            // 过滤出可用的时间段
            List<Map<String, String>> availableSlots = filterAvailableSlots(timeSlots, daySchedules, slotDuration);

            return R.ok().put("data", availableSlots)
                    .put("classroomId", classroomId)
                    .put("date", date)
                    .put("totalSlots", availableSlots.size());
        } catch (Exception e) {
            return R.error("获取可用时间段失败: " + e.getMessage());
        }
    }

    // 生成标准时间段
    private List<Map<String, String>> generateTimeSlots() {
        List<Map<String, String>> slots = new java.util.ArrayList<>();
        String[] times = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00",
                "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};

        for (int i = 0; i < times.length - 1; i++) {
            slots.add(Map.of(
                    "startTime", times[i],
                    "endTime", times[i + 1],
                    "slot", (i + 1) + "-" + (i + 2),
                    "duration", "60分钟"
            ));
        }
        return slots;
    }

    // 过滤可用时间段
    private List<Map<String, String>> filterAvailableSlots(List<Map<String, String>> timeSlots,
                                                           List<CourseSchedule> schedules, int duration) {
        return timeSlots.stream()
                .filter(slot -> {
                    String startTime = slot.get("startTime");
                    String endTime = slot.get("endTime");

                    // 检查该时间段是否被占用
                    return schedules.stream()
                            .noneMatch(schedule -> {
                                String scheduleStart = schedule.getStartTime().toString();
                                String scheduleEnd = schedule.getEndTime().toString();

                                // 时间冲突检测逻辑
                                return !(endTime.compareTo(scheduleStart) <= 0 ||
                                        startTime.compareTo(scheduleEnd) >= 0);
                            });
                })
                .collect(Collectors.toList());
    }

    // 批量导入课程安排
    @PostMapping("/batch-import")
    public R batchImportTimetable(@RequestBody List<CourseSchedule> schedules) {
        try {
            if (schedules == null || schedules.isEmpty()) {
                return R.error("导入数据不能为空");
            }

            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new java.util.ArrayList<>();

            for (int i = 0; i < schedules.size(); i++) {
                try {
                    CourseSchedule schedule = schedules.get(i);
                    boolean success = courseService.addCourseSchedule(schedule);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("第" + (i + 1) + "条数据导入失败");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("第" + (i + 1) + "条数据导入失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = Map.of(
                    "total", schedules.size(),
                    "success", successCount,
                    "fail", failCount,
                    "errors", errorMessages
            );

            return R.ok().put("data", result).put("message",
                    String.format("导入完成：成功%d条，失败%d条", successCount, failCount));
        } catch (Exception e) {
            return R.error("批量导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/student/week")
    public R getStudentTimetableByWeek(@RequestParam String studentId,
                                       @RequestParam(required = false) Integer week) {
        try {
            int weekNumber = week != null ? week : courseService.getCurrentWeek();
            List<CourseSchedule> timetable = courseService.getStudentTimetableByWeek(studentId, weekNumber);

            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);
            WeekCalculator.AcademicYearSemester academicYearSemester = courseService.getCurrentAcademicYearSemester();

            Map<String, Object> result = Map.of(
                    "weekNumber", weekNumber,
                    "timetable", timetable,
                    "academicYear", academicYearSemester.getAcademicYear(),
                    "semester", academicYearSemester.getSemester(),
                    "weekStartDate", weekRange.getStartDate(),
                    "weekEndDate", weekRange.getEndDate(),
                    "studentId", studentId
            );

            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("获取学生周课表失败: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/week")
    public R getTeacherTimetableByWeek(@RequestParam String teacherId,
                                       @RequestParam(required = false) Integer week) {
        try {
            int weekNumber = week != null ? week : courseService.getCurrentWeek();
            List<CourseSchedule> timetable = courseService.getTeacherTimetableByWeek(teacherId, weekNumber);

            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

            Map<String, Object> result = Map.of(
                    "weekNumber", weekNumber,
                    "timetable", timetable,
                    "weekStartDate", weekRange.getStartDate(),
                    "weekEndDate", weekRange.getEndDate(),
                    "teacherId", teacherId
            );

            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("获取教师周课表失败: " + e.getMessage());
        }
    }

    @GetMapping("/current-week")
    public R getCurrentWeekInfo() {
        try {
            int currentWeek = courseService.getCurrentWeek();
            WeekCalculator.AcademicYearSemester academicYearSemester = courseService.getCurrentAcademicYearSemester();
            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(currentWeek);

            Map<String, Object> result = Map.of(
                    "currentWeek", currentWeek,
                    "academicYear", academicYearSemester.getAcademicYear(),
                    "semester", academicYearSemester.getSemester(),
                    "weekStartDate", weekRange.getStartDate(),
                    "weekEndDate", weekRange.getEndDate(),
                    "currentDate", LocalDate.now()
            );

            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("获取周次信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/week-number")
    public R getWeekNumberByDate(@RequestParam String date) {
        try {
            LocalDate queryDate = LocalDate.parse(date);
            int weekNumber = courseService.getWeekNumber(queryDate);
            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

            return R.ok().put("data", Map.of(
                    "date", date,
                    "weekNumber", weekNumber,
                    "weekStartDate", weekRange.getStartDate(),
                    "weekEndDate", weekRange.getEndDate()
            ));
        } catch (Exception e) {
            return R.error("计算周次失败: " + e.getMessage());
        }
    }

    // 新增：获取班级周课表
    @GetMapping("/class/week")
    public R getClassTimetableByWeek(@RequestParam String classId,
                                     @RequestParam(required = false) Integer week) {
        try {
            int weekNumber = week != null ? week : courseService.getCurrentWeek();
            List<CourseSchedule> timetable = courseService.getClassTimetableByWeek(classId, weekNumber);

            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

            // 按天分组
            Map<Integer, List<CourseSchedule>> groupedByDay = timetable.stream()
                    .collect(Collectors.groupingBy(schedule ->
                            schedule.getDayOfWeek() != null ?
                                    schedule.getDayOfWeek() :
                                    schedule.getScheduleDate().getDayOfWeek().getValue()
                    ));

            Map<String, Object> result = Map.of(
                    "weekNumber", weekNumber,
                    "timetable", groupedByDay,
                    "weekStartDate", weekRange.getStartDate(),
                    "weekEndDate", weekRange.getEndDate(),
                    "classId", classId
            );

            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("获取班级周课表失败: " + e.getMessage());
        }
    }
}