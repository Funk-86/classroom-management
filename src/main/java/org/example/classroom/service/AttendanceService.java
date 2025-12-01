package org.example.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.AttendanceRecord;
import org.example.classroom.entity.AttendanceSession;

import java.math.BigDecimal;
import java.util.List;

public interface AttendanceService extends IService<AttendanceSession> {

    // 教师发起签到
    AttendanceSession createSession(AttendanceSession session);

    // 获取签到活动详情
    AttendanceSession getSessionDetail(String sessionId);

    // 获取教师的签到活动列表
    List<AttendanceSession> getTeacherSessions(String teacherId, Integer limit);

    // 获取课程的进行中签到活动
    AttendanceSession getActiveSessionByCourse(String courseId);

    // 获取课程的所有签到活动列表（包括历史）
    List<AttendanceSession> getCourseSessions(String courseId);

    // 获取学生的可签到活动列表
    List<AttendanceSession> getActiveSessionsForStudent(String studentId);

    // 学生签到
    AttendanceRecord checkIn(String sessionId, String studentId, BigDecimal latitude, BigDecimal longitude);

    // 获取签到活动的签到记录列表
    List<AttendanceRecord> getSessionRecords(String sessionId);

    // 获取学生的签到记录
    List<AttendanceRecord> getStudentRecords(String studentId, Integer limit);

    // 更新签到记录状态（教师手动修改）
    boolean updateRecordStatus(String recordId, String studentId, Integer status);

    // 计算两点之间的距离（米）
    double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2);
}

