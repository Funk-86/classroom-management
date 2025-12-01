package org.example.classroom.controller;

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
                             @RequestParam(required = false) Integer dayOfWeek,
                             @RequestParam(required = false) Integer scheduleType,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            return R.ok().put("data", courseService.getSchedulesWithDetail(courseId, classroomId, campusId,
                    dayOfWeek, scheduleType, start, end, page, size));
        } catch (Exception e) {
            return R.error("获取课程安排列表失败: " + e.getMessage());
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

    // 批量分配课程给班级
    @PostMapping("/assign/class")
    public R assignCourseToClass(@RequestParam String courseId,
                                 @RequestParam String classId) {
        try {
            boolean success = courseService.assignCourseToClass(courseId, classId, getCurrentUserId());
            if (success) {
                return R.ok("分配课程给班级成功");
            } else {
                return R.error("分配课程给班级失败");
            }
        } catch (Exception e) {
            return R.error("分配课程给班级失败: " + e.getMessage());
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