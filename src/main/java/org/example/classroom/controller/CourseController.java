package org.example.classroom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import org.example.classroom.dto.R;
import org.example.classroom.entity.Course;
import org.example.classroom.entity.CourseSchedule;
import org.example.classroom.entity.StudentCourse;
import org.example.classroom.entity.User;
import org.example.classroom.mapper.UserMapper;
import org.example.classroom.service.CourseService;
import org.example.classroom.util.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private org.example.classroom.service.SemesterService semesterService;

    // 获取课程列表
    @GetMapping("/list")
    public R getCourseList(@RequestParam(required = false) String collegeId,
                           @RequestParam(required = false) String academicYear,
                           @RequestParam(required = false) Integer semester,
                           @RequestParam(required = false) Integer courseType,
                           @RequestParam(required = false) String teacherId,
                           @RequestParam(required = false) String studentId,
                           @RequestParam(required = false) Integer enrollmentStatus, // 选课状态(0未选,1已选,2退选,3完成)
                           @RequestParam(required = false) Integer isRequired, // 是否必修(0选修,1必修)
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size) {
        try {
            return R.ok().put("data", courseService.getCoursesWithDetail(collegeId, academicYear, semester,
                    courseType, teacherId, studentId, enrollmentStatus, isRequired, keyword, page, size));
        } catch (Exception e) {
            return R.error("获取课程列表失败: " + e.getMessage());
        }
    }

    // 获取学生可选课程列表（未选的选修课）
    @GetMapping("/available/{studentId}")
    public R getAvailableCourses(@PathVariable String studentId,
                                 @RequestParam(required = false) String academicYear,
                                 @RequestParam(required = false) Integer semester) {
        try {
            List<Course> availableCourses = courseService.getAvailableCoursesForStudent(studentId, academicYear, semester);
            return R.ok().put("data", availableCourses);
        } catch (Exception e) {
            return R.error("获取可选课程失败: " + e.getMessage());
        }
    }

    // 获取学生必修课程列表（管理员分配的）
    @GetMapping("/required/{studentId}")
    public R getRequiredCourses(@PathVariable String studentId,
                                @RequestParam(required = false) String academicYear,
                                @RequestParam(required = false) Integer semester) {
        try {
            List<Course> requiredCourses = courseService.getRequiredCoursesForStudent(studentId, academicYear, semester);
            return R.ok().put("data", requiredCourses);
        } catch (Exception e) {
            return R.error("获取必修课程失败: " + e.getMessage());
        }
    }

    // 获取课程详情
    @GetMapping("/{courseId}")
    public R getCourseDetail(@PathVariable String courseId) {
        try {
            Course course = courseService.getCourseWithDetail(courseId);
            if (course != null) {
                return R.ok().put("data", course);
            } else {
                return R.error("课程不存在");
            }
        } catch (Exception e) {
            return R.error("获取课程详情失败: " + e.getMessage());
        }
    }

    // 添加课程
    @PostMapping("/add")
    public R addCourse(@RequestBody Course course,
                       @RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String classId,
                       @RequestParam(required = false) Boolean assignToStudent,
                       @RequestParam(required = false) Boolean assignToClass) {
        try {
            // 验证必填字段
            if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
                return R.error("课程代码不能为空");
            }
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                return R.error("课程名称不能为空");
            }
            if (course.getAcademicYear() == null || course.getAcademicYear().trim().isEmpty()) {
                return R.error("学年不能为空");
            }
            if (course.getSemester() == null) {
                return R.error("学期不能为空");
            }

            // 如果指定了分配给学生
            if (assignToStudent != null && assignToStudent && studentId != null) {
                course.setStudentId(studentId);
                course.setEnrollmentStatus(1);
                course.setIsRequired(1);
                course.setAssignedBy(getCurrentUserId());
                course.setAssignedAt(LocalDateTime.now());
            }

            // 如果指定了分配给班级
            if (assignToClass != null && assignToClass && classId != null) {
                course.setClassId(classId);
                course.setIsRequired(1);
                course.setAssignedBy(getCurrentUserId());
                course.setAssignedAt(LocalDateTime.now());
            }

            boolean success = courseService.addCourse(course);
            if (success) {
                return R.ok("添加课程成功");
            } else {
                return R.error("添加课程失败");
            }
        } catch (Exception e) {
            return R.error("添加课程失败: " + e.getMessage());
        }
    }


    // 更新课程
    @PutMapping("/update")
    public R updateCourse(@RequestBody Course course) {
        try {
            if (course.getCourseId() == null || course.getCourseId().trim().isEmpty()) {
                return R.error("课程ID不能为空");
            }

            boolean success = courseService.updateCourse(course);
            if (success) {
                return R.ok("更新课程成功");
            } else {
                return R.error("更新课程失败");
            }
        } catch (Exception e) {
            return R.error("更新课程失败: " + e.getMessage());
        }
    }

    // 删除课程
    @DeleteMapping("/delete/{courseId}")
    public R deleteCourse(@PathVariable String courseId) {
        try {
            boolean success = courseService.deleteCourse(courseId);
            if (success) {
                return R.ok("删除课程成功");
            } else {
                return R.error("删除课程失败");
            }
        } catch (Exception e) {
            return R.error("删除课程失败: " + e.getMessage());
        }
    }

    @PostMapping("/assign")
    public R assignCourseToStudent(@RequestParam String courseId,
                                   @RequestParam String studentId,
                                   @RequestParam(defaultValue = "1") Integer isRequired) {
        try {
            if (courseId == null || courseId.trim().isEmpty()) {
                return R.error("课程ID不能为空");
            }
            if (studentId == null || studentId.trim().isEmpty()) {
                return R.error("学生ID不能为空");
            }

            boolean success = courseService.assignCourseToStudent(courseId, studentId, isRequired, getCurrentUserId());
            if (success) {
                return R.ok("分配课程成功");
            } else {
                return R.error("分配课程失败");
            }
        } catch (Exception e) {
            return R.error("分配课程失败: " + e.getMessage());
        }
    }

    // 批量分配课程给学生
    @PostMapping("/assign/batch")
    public R batchAssignCourses(@RequestBody List<BatchAssignRequest> requests) {
        try {
            if (requests == null || requests.isEmpty()) {
                return R.error("分配列表不能为空");
            }

            int successCount = courseService.batchAssignCourses(requests, getCurrentUserId());
            return R.ok("批量分配完成").put("data", successCount).put("total", requests.size());
        } catch (Exception e) {
            return R.error("批量分配失败: " + e.getMessage());
        }
    }

    // 获取课程安排列表
    @GetMapping("/schedule/list")
    public R getScheduleList(@RequestParam(required = false) String courseId,
                             @RequestParam(required = false) String classroomId,
                             @RequestParam(required = false) String campusId,
                             @RequestParam(required = false) String classId,
                             @RequestParam(required = false) String semesterId,
                             @RequestParam(required = false) Integer week,
                             @RequestParam(required = false) Integer dayOfWeek,
                             @RequestParam(required = false) Integer scheduleType,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            // 如果提供了classId，使用班级课表查询
            if (classId != null && !classId.trim().isEmpty()) {
                // 如果有week参数且不为空，使用指定的周次
                int weekNumber;
                if (week != null && week > 0) {
                    weekNumber = week;
                } else {
                    // 如果没有week参数或week为0，使用当前周次
                    weekNumber = courseService.getCurrentWeek();
                }

                // 如果提供了semesterId，使用带学期过滤的查询方法
                List<CourseSchedule> schedules;
                if (semesterId != null && !semesterId.trim().isEmpty()) {
                    // 使用SQL层面的semester_id过滤，更高效
                    schedules = courseService.getClassTimetableByWeekAndSemester(classId, semesterId, weekNumber);
                } else {
                    // 如果没有提供semesterId，使用原来的方法
                    schedules = courseService.getClassTimetableByWeek(classId, weekNumber);
                }

                return R.ok().put("data", schedules);
            }

            // 如果提供了semesterId，通过学期查询
            if (semesterId != null && !semesterId.trim().isEmpty()) {
                // 获取学期信息
                org.example.classroom.entity.Semester semester = semesterService.getById(semesterId);
                if (semester == null) {
                    return R.error("学期不存在");
                }

                // 从学期名称中提取学期类型（1=春季，2=秋季）
                Integer semesterType = null;
                if (semester.getName() != null) {
                    String name = semester.getName();
                    // 支持多种格式：春季、秋季、春季学期、秋季学期等
                    if (name.contains("春季") || name.contains("春")) {
                        semesterType = 1;
                    } else if (name.contains("秋季") || name.contains("秋")) {
                        semesterType = 2;
                    }
                }
                // lambda 中使用的变量需要是 final / effectively final，这里做一次拷贝
                final Integer semesterTypeFinal = semesterType;

                // 根据学期信息（academicYear 和 semester）过滤课程安排
                // 先查询所有课程安排，然后过滤出属于该学期的课程
                // 使用较大的分页大小来获取所有数据，然后手动过滤
                IPage<CourseSchedule> allSchedules = courseService.getSchedulesWithDetail(
                        courseId, classroomId, campusId, dayOfWeek, scheduleType, start, end, 1, 10000);

                // 过滤出属于指定学期的课程安排
                List<CourseSchedule> filteredSchedules = allSchedules.getRecords().stream()
                        .filter(schedule -> {
                            // 通过 courseId 获取课程信息
                            Course course = courseService.getById(schedule.getCourseId());
                            if (course == null) {
                                return false;
                            }

                            // 检查课程的学年和学期是否匹配
                            boolean yearMatch = semester.getAcademicYear() != null &&
                                    semester.getAcademicYear().equals(course.getAcademicYear());
                            boolean semesterMatch = semesterTypeFinal != null &&
                                    semesterTypeFinal.equals(course.getSemester());

                            return yearMatch && semesterMatch;
                        })
                        .collect(java.util.stream.Collectors.toList());

                // 如果指定了周次，进一步过滤
                if (week != null && week > 0) {
                    filteredSchedules = filteredSchedules.stream()
                            .filter(schedule -> {
                                if (schedule.getScheduleType() != null && schedule.getScheduleType() == 0) {
                                    // 每周重复的课程，检查周次范围
                                    Integer startWeek = schedule.getStartWeek();
                                    Integer endWeek = schedule.getEndWeek();
                                    return startWeek != null && endWeek != null &&
                                            week >= startWeek && week <= endWeek;
                                } else if (schedule.getScheduleType() != null && schedule.getScheduleType() == 1) {
                                    // 单次安排的课程，需要根据日期计算周次
                                    // 这里简化处理，如果指定了周次，单次安排需要根据日期判断
                                    return true; // 暂时返回true，可以根据需要进一步实现
                                }
                                return true;
                            })
                            .collect(java.util.stream.Collectors.toList());
                }

                // 创建分页结果
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<CourseSchedule> resultPage =
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
                resultPage.setRecords(filteredSchedules);
                resultPage.setTotal(filteredSchedules.size());

                return R.ok().put("data", resultPage);
            }

            return R.ok().put("data", courseService.getSchedulesWithDetail(courseId, classroomId, campusId,
                    dayOfWeek, scheduleType, start, end, page, size));
        } catch (Exception e) {
            return R.error("获取课程安排列表失败: " + e.getMessage());
        }
    }

    // 获取课程安排详情
    @GetMapping("/schedule/{scheduleId}")
    public R getScheduleDetail(@PathVariable String scheduleId) {
        try {
            CourseSchedule schedule = courseService.getScheduleWithDetail(scheduleId);
            if (schedule != null) {
                return R.ok().put("data", schedule);
            } else {
                return R.error("课程安排不存在");
            }
        } catch (Exception e) {
            return R.error("获取课程安排详情失败: " + e.getMessage());
        }
    }

    // 添加课程安排
    @PostMapping("/schedule/add")
    public R addCourseSchedule(@RequestBody CourseSchedule schedule) {
        try {
            boolean success = courseService.addCourseSchedule(schedule);
            if (success) {
                return R.ok("添加课程安排成功");
            } else {
                return R.error("添加课程安排失败");
            }
        } catch (Exception e) {
            return R.error("添加课程安排失败: " + e.getMessage());
        }
    }

    // 更新课程安排
    @PutMapping("/schedule/update")
    public R updateCourseSchedule(@RequestBody CourseSchedule schedule) {
        try {
            if (schedule.getScheduleId() == null || schedule.getScheduleId().trim().isEmpty()) {
                return R.error("安排ID不能为空");
            }

            boolean success = courseService.updateCourseSchedule(schedule);
            if (success) {
                return R.ok("更新课程安排成功");
            } else {
                return R.error("更新课程安排失败");
            }
        } catch (Exception e) {
            return R.error("更新课程安排失败: " + e.getMessage());
        }
    }


    // 删除课程安排
    @DeleteMapping("/schedule/delete/{scheduleId}")
    public R deleteCourseSchedule(@PathVariable String scheduleId) {
        try {
            boolean success = courseService.deleteCourseSchedule(scheduleId);
            if (success) {
                return R.ok("删除课程安排成功");
            } else {
                return R.error("删除课程安排失败");
            }
        } catch (Exception e) {
            return R.error("删除课程安排失败: " + e.getMessage());
        }
    }

    // 获取教师课表
    @GetMapping("/timetable/teacher")
    public R getTeacherTimetable(@RequestParam String teacherId,
                                 @RequestParam(required = false) String date) {
        try {
            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            List<CourseSchedule> timetable = courseService.getTeacherTimetable(teacherId, queryDate);
            return R.ok().put("data", timetable);
        } catch (Exception e) {
            return R.error("获取教师课表失败: " + e.getMessage());
        }
    }

    // 检查时间冲突
    @PostMapping("/schedule/check-conflict")
    public R checkScheduleConflict(@RequestBody CourseSchedule schedule) {
        try {
            boolean hasConflict = courseService.checkScheduleConflict(schedule);
            return R.ok().put("data", hasConflict).put("message", hasConflict ? "存在时间冲突" : "无时间冲突");
        } catch (Exception e) {
            return R.error("检查时间冲突失败: " + e.getMessage());
        }
    }

    // 获取学生选课列表
    @GetMapping("/enrollment/list")
    public R getStudentCourseList(@RequestParam(required = false) String studentId,
                                  @RequestParam(required = false) String courseId,
                                  @RequestParam(required = false) String collegeId,
                                  @RequestParam(required = false) String academicYear,
                                  @RequestParam(required = false) Integer semester,
                                  @RequestParam(required = false) Integer enrollmentStatus,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        try {
            return R.ok().put("data", courseService.getStudentCoursesWithDetail(studentId, courseId, collegeId,
                    academicYear, semester, enrollmentStatus, page, size));
        } catch (Exception e) {
            return R.error("获取学生选课列表失败: " + e.getMessage());
        }
    }

    // 学生选课
    @PostMapping("/enrollment/enroll")
    public R enrollCourse(@RequestBody StudentCourse studentCourse) {
        try {
            if (studentCourse.getStudentId() == null || studentCourse.getStudentId().trim().isEmpty()) {
                return R.error("学生ID不能为空");
            }
            if (studentCourse.getCourseId() == null || studentCourse.getCourseId().trim().isEmpty()) {
                return R.error("课程ID不能为空");
            }
            if (studentCourse.getAcademicYear() == null || studentCourse.getAcademicYear().trim().isEmpty()) {
                return R.error("学年不能为空");
            }
            if (studentCourse.getSemester() == null) {
                return R.error("学期不能为空");
            }

            boolean success = courseService.enrollCourse(studentCourse);
            if (success) {
                return R.ok("选课成功");
            } else {
                return R.error("选课失败");
            }
        } catch (Exception e) {
            return R.error("选课失败: " + e.getMessage());
        }
    }

    // 学生退选
    @PutMapping("/enrollment/withdraw/{studentCourseId}")
    public R withdrawCourse(@PathVariable String studentCourseId) {
        try {
            boolean success = courseService.withdrawCourse(studentCourseId);
            if (success) {
                return R.ok("退选成功");
            } else {
                return R.error("退选失败");
            }
        } catch (Exception e) {
            return R.error("退选失败: " + e.getMessage());
        }
    }

    // 更新课程成绩
    @PutMapping("/enrollment/score")
    public R updateCourseScore(@RequestParam String studentCourseId,
                               @RequestParam BigDecimal score) {
        try {
            if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
                return R.error("成绩必须在0-100之间");
            }

            boolean success = courseService.updateCourseScore(studentCourseId, score);
            if (success) {
                return R.ok("更新成绩成功");
            } else {
                return R.error("更新成绩失败");
            }
        } catch (Exception e) {
            return R.error("更新成绩失败: " + e.getMessage());
        }
    }

    // 获取学生个人选课列表（包括班级课程和已选课程）
    @GetMapping("/enrollment/student/{studentId}")
    public R getStudentPersonalCourses(@PathVariable String studentId,
                                       @RequestParam(required = false) String academicYear,
                                       @RequestParam(required = false) Integer semester) {
        try {
            // 获取学生信息，包括classId
            User user = userMapper.selectById(studentId);
            if (user == null) {
                return R.error("学生不存在");
            }

            String classId = user.getClassId();
            System.out.println("=== 获取学生课程列表 ===");
            System.out.println("学生ID: " + studentId);
            System.out.println("班级ID: " + classId);
            System.out.println("学年: " + academicYear);
            System.out.println("学期: " + semester);

            // 处理空字符串参数
            String yearParam = (academicYear != null && academicYear.trim().isEmpty()) ? null : academicYear;

            // 获取所有课程：包括班级课程和已选课程
            List<Course> allCourses = courseService.getAllStudentCourses(studentId, classId, yearParam, semester);
            System.out.println("查询到的课程数量: " + (allCourses != null ? allCourses.size() : 0));

            // 转换为统一格式，方便前端统一处理
            List<Map<String, Object>> courseList = allCourses.stream().map(course -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", course.getCourseId()); // 用于wx:key
                map.put("courseId", course.getCourseId());
                map.put("courseName", course.getCourseName());
                map.put("courseCode", course.getCourseCode());
                map.put("creditHours", course.getCreditHours());
                map.put("courseType", course.getCourseType());
                map.put("courseTypeText", course.getCourseTypeText() != null ?
                        course.getCourseTypeText() : getCourseTypeText(course.getCourseType()));
                map.put("teacherName", course.getTeacherName() != null ? course.getTeacherName() : "未知教师");
                map.put("enrollmentStatus", 1); // 默认已选
                map.put("enrollmentStatusText", "已选");
                return map;
            }).collect(java.util.stream.Collectors.toList());

            return R.ok().put("data", courseList);
        } catch (Exception e) {
            return R.error("获取学生选课列表失败: " + e.getMessage());
        }
    }

    // 课程类型文本转换辅助方法
    private String getCourseTypeText(Integer courseType) {
        if (courseType == null) return "未知类型";
        switch (courseType) {
            case 0: return "必修课";
            case 1: return "选修课";
            case 2: return "实践课";
            default: return "未知类型";
        }
    }

    // 检查是否已选某课程
    @GetMapping("/enrollment/check")
    public R checkCourseEnrollment(@RequestParam String studentId,
                                   @RequestParam String courseId,
                                   @RequestParam String academicYear,
                                   @RequestParam Integer semester) {
        try {
            boolean enrolled = courseService.checkCourseEnrollment(studentId, courseId, academicYear, semester);
            return R.ok().put("data", enrolled).put("message", enrolled ? "已选该课程" : "未选该课程");
        } catch (Exception e) {
            return R.error("检查选课状态失败: " + e.getMessage());
        }
    }

    public static class BatchAssignRequest {
        private String courseId;
        private String studentId;
        private Integer isRequired;

        // Getter和Setter
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public Integer getIsRequired() { return isRequired; }
        public void setIsRequired(Integer isRequired) { this.isRequired = isRequired; }
    }

    // 分配课程给班级（支持多班级）
    @PostMapping("/assign/class")
    public R assignCourseToClass(@RequestParam String courseId,
                                 @RequestParam String classId) {
        try {
            boolean success = courseService.assignCourseToClass(courseId, classId, getCurrentUserId());
            if (success) {
                return R.ok("分配课程到班级成功");
            } else {
                return R.error("分配课程到班级失败");
            }
        } catch (Exception e) {
            return R.error("分配课程到班级失败: " + e.getMessage());
        }
    }

    // 批量分配课程给多个班级
    @PostMapping("/assign/classes")
    public R batchAssignCourseToClasses(@RequestParam String courseId,
                                        @RequestBody List<String> classIds) {
        try {
            boolean success = courseService.batchAssignCourseToClasses(courseId, classIds, getCurrentUserId());
            if (success) {
                return R.ok("批量分配课程到班级成功");
            } else {
                return R.error("批量分配课程到班级失败");
            }
        } catch (Exception e) {
            return R.error("批量分配课程到班级失败: " + e.getMessage());
        }
    }

    // 移除课程的班级关联
    @DeleteMapping("/remove/class")
    public R removeCourseFromClass(@RequestParam String courseId,
                                   @RequestParam String classId) {
        try {
            boolean success = courseService.removeCourseFromClass(courseId, classId);
            if (success) {
                return R.ok("移除课程班级关联成功");
            } else {
                return R.error("移除课程班级关联失败");
            }
        } catch (Exception e) {
            return R.error("移除课程班级关联失败: " + e.getMessage());
        }
    }

    // 获取课程的所有关联班级
    @GetMapping("/{courseId}/classes")
    public R getCourseClasses(@PathVariable String courseId) {
        try {
            return R.ok().put("data", courseService.getCourseClasses(courseId));
        } catch (Exception e) {
            return R.error("获取课程班级列表失败: " + e.getMessage());
        }
    }

    // 获取班级的所有关联课程
    @GetMapping("/class/{classId}/courses")
    public R getClassCourses(@PathVariable String classId) {
        try {
            return R.ok().put("data", courseService.getClassCourses(classId));
        } catch (Exception e) {
            return R.error("获取班级课程列表失败: " + e.getMessage());
        }
    }

    private String getCurrentUserId() {
        // 从请求头中获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return currentUserUtil.getCurrentUserId(token);
    }

}