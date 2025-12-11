package org.example.classroom.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 扫码查看教室实时状态返回体
 */
@Data
public class ClassroomRealtimeResponse {
    private String classroomId;
    private String classroomName;
    private String buildingId;
    private Integer status;
    private String statusText;

    /**
     * 当前占用类型：NONE/COURSE_SCHEDULE/RESERVATION
     */
    private String activityType;

    /**
     * 当前课程信息（若存在）
     */
    private CurrentCourse currentCourse;

    /**
     * 当前预约信息（若无课程但有预约）
     */
    private CurrentReservation currentReservation;

    /**
     * 签到汇总（若存在进行中的签到活动）
     */
    private AttendanceSummary attendance;

    @Data
    public static class CurrentCourse {
        private String scheduleId;
        private String courseId;
        private String courseName;
        private String teacherId;
        private String teacherName;
        private Integer scheduleType;
        private Integer dayOfWeek;
        private LocalDate scheduleDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer startWeek;
        private Integer endWeek;
    }

    @Data
    public static class CurrentReservation {
        private String reservationId;
        private String userId;
        private String userName;
        private String purpose;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    public static class AttendanceSummary {
        private String sessionId;
        private String sessionTitle;
        private Integer totalStudents;
        private Integer checkinCount;
        private Integer absentCount;
        private List<StudentLite> absentStudents;
    }

    @Data
    public static class StudentLite {
        private String studentId;
        private String studentName;
    }
}

