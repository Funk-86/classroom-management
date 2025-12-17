package org.example.classroom.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classroom.dto.R;
import org.example.classroom.entity.AttendanceRecord;
import org.example.classroom.entity.AttendanceSession;
import org.example.classroom.service.AttendanceService;
import org.example.classroom.util.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @Autowired
    private org.example.classroom.service.CourseService courseService;

    // 获取当前用户ID的辅助方法
    private String getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求信息");
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return currentUserUtil.getCurrentUserId(token);
    }

    // 教师发起签到
    @PostMapping("/session/create")
    public R createSession(@RequestBody Map<String, Object> requestData) {
        try {
            String teacherId = getCurrentUserId();

            // 检查是否有多个班级ID
            Object classIdsObj = requestData.get("classIds");
            List<String> classIds = null;
            if (classIdsObj instanceof List) {
                classIds = (List<String>) classIdsObj;
            }

            // 转换为AttendanceSession对象
            AttendanceSession session = new AttendanceSession();
            session.setCourseId((String) requestData.get("courseId"));
            session.setSessionTitle((String) requestData.get("sessionTitle"));

            Object latObj = requestData.get("latitude");
            Object lonObj = requestData.get("longitude");
            if (latObj != null) {
                session.setLatitude(new java.math.BigDecimal(latObj.toString()));
            }
            if (lonObj != null) {
                session.setLongitude(new java.math.BigDecimal(lonObj.toString()));
            }

            Object radiusObj = requestData.get("radius");
            if (radiusObj != null) {
                session.setRadius(radiusObj instanceof Integer ? (Integer) radiusObj : Integer.parseInt(radiusObj.toString()));
            }

            // 解析时间
            String startTimeStr = (String) requestData.get("startTime");
            String endTimeStr = (String) requestData.get("endTime");
            if (startTimeStr != null) {
                try {
                    session.setStartTime(java.time.LocalDateTime.parse(startTimeStr,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    // 尝试其他格式
                    session.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
                }
            }
            if (endTimeStr != null) {
                try {
                    session.setEndTime(java.time.LocalDateTime.parse(endTimeStr,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    // 尝试其他格式
                    session.setEndTime(java.time.LocalDateTime.parse(endTimeStr));
                }
            }

            session.setTeacherId(teacherId);

            // 统一处理：只创建一个签到活动，不指定classId（表示所有关联班级）
            // 如果指定了单个班级，则设置classId；如果多个班级或未指定，则不设置classId（表示所有班级）
            if (classIds != null && classIds.size() == 1) {
                // 单个班级
                session.setClassId(classIds.get(0));
            } else if (classIds == null || classIds.isEmpty()) {
                // 未选择班级，使用传入的classId（可能为null，表示所有班级）
                String classId = (String) requestData.get("classId");
                session.setClassId(classId);
            }
            // 多个班级时，不设置classId，表示所有关联班级

            AttendanceSession created = attendanceService.createSession(session);
            return R.ok().put("data", created);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息
            return R.error("发起签到失败: " + e.getMessage());
        }
    }

    // 获取签到活动详情
    @GetMapping("/session/{sessionId}")
    public R getSessionDetail(@PathVariable String sessionId) {
        try {
            AttendanceSession session = attendanceService.getSessionDetail(sessionId);
            if (session == null) {
                return R.error("签到活动不存在");
            }
            return R.ok().put("data", session);
        } catch (Exception e) {
            return R.error("获取签到活动失败: " + e.getMessage());
        }
    }

    // 获取教师的签到活动列表
    @GetMapping("/session/teacher/list")
    public R getTeacherSessions(@RequestParam(defaultValue = "20") Integer limit) {
        try {
            String teacherId = getCurrentUserId();
            List<AttendanceSession> sessions = attendanceService.getTeacherSessions(teacherId, limit);
            return R.ok().put("data", sessions);
        } catch (Exception e) {
            return R.error("获取签到活动列表失败: " + e.getMessage());
        }
    }

    // 获取课程的进行中签到活动
    @GetMapping("/session/course/{courseId}/active")
    public R getActiveSessionByCourse(@PathVariable String courseId) {
        try {
            AttendanceSession session = attendanceService.getActiveSessionByCourse(courseId);
            return R.ok().put("data", session);
        } catch (Exception e) {
            return R.error("获取签到活动失败: " + e.getMessage());
        }
    }

    // 获取课程的所有签到活动列表（包括历史）
    @GetMapping("/session/course/{courseId}/list")
    public R getCourseSessions(@PathVariable String courseId) {
        try {
            List<AttendanceSession> sessions = attendanceService.getCourseSessions(courseId);
            return R.ok().put("data", sessions);
        } catch (Exception e) {
            return R.error("获取签到活动列表失败: " + e.getMessage());
        }
    }

    // 获取学生的可签到活动列表
    @GetMapping("/session/student/active")
    public R getActiveSessionsForStudent() {
        try {
            String studentId = getCurrentUserId();
            List<AttendanceSession> sessions = attendanceService.getActiveSessionsForStudent(studentId);
            return R.ok().put("data", sessions);
        } catch (Exception e) {
            return R.error("获取签到活动列表失败: " + e.getMessage());
        }
    }

    // 学生签到
    @PostMapping("/checkin")
    public R checkIn(@RequestBody Map<String, Object> params) {
        try {
            String sessionId = (String) params.get("sessionId");
            BigDecimal latitude = new BigDecimal(params.get("latitude").toString());
            BigDecimal longitude = new BigDecimal(params.get("longitude").toString());

            String studentId = getCurrentUserId();
            AttendanceRecord record = attendanceService.checkIn(sessionId, studentId, latitude, longitude);
            return R.ok().put("data", record);
        } catch (Exception e) {
            return R.error("签到失败: " + e.getMessage());
        }
    }

    // 获取签到活动的签到记录列表（自动合并相关活动的记录）
    @GetMapping("/record/session/{sessionId}")
    public R getSessionRecords(@PathVariable String sessionId) {
        try {
            // 使用合并查询，自动合并同一批创建的多班级签到活动记录
            List<AttendanceRecord> records = attendanceService.getRelatedSessionsRecords(sessionId);
            return R.ok().put("data", records);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细异常堆栈
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + ": " + (e.getCause() != null ? e.getCause().getMessage() : "未知错误");
            }
            return R.error("获取签到记录失败: " + errorMsg);
        }
    }

    // 获取学生的签到记录
    @GetMapping("/record/student")
    public R getStudentRecords(@RequestParam(defaultValue = "20") Integer limit) {
        try {
            String studentId = getCurrentUserId();
            List<AttendanceRecord> records = attendanceService.getStudentRecords(studentId, limit);
            return R.ok().put("data", records);
        } catch (Exception e) {
            return R.error("获取签到记录失败: " + e.getMessage());
        }
    }

    // 获取课程的关联班级列表（用于签到选择）
    @GetMapping("/course/{courseId}/classes")
    public R getCourseClasses(@PathVariable String courseId) {
        try {
            return R.ok().put("data", courseService.getCourseClasses(courseId));
        } catch (Exception e) {
            return R.error("获取课程班级列表失败: " + e.getMessage());
        }
    }

    // 更新签到记录状态（教师手动修改）
    @PostMapping("/record/update")
    public R updateRecordStatus(@RequestBody Map<String, Object> params) {
        try {
            String recordId = (String) params.get("recordId");
            String studentId = (String) params.get("studentId");
            String sessionId = (String) params.get("sessionId"); // 添加sessionId参数
            Integer status = (Integer) params.get("status");

            if (studentId == null || status == null) {
                return R.error("参数不完整");
            }

            // 如果recordId是temp_开头，必须提供sessionId
            if (recordId != null && recordId.startsWith("temp_") && sessionId == null) {
                return R.error("参数不完整：缺少签到活动ID");
            }

            boolean success = attendanceService.updateRecordStatus(recordId, studentId, sessionId, status);
            if (success) {
                return R.ok();
            } else {
                return R.error("更新失败");
            }
        } catch (Exception e) {
            return R.error("更新签到状态失败: " + e.getMessage());
        }
    }
}

