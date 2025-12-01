package org.example.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 教室占用信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOccupationInfo {
    /**
     * 占用类型
     */
    private OccupationType occupationType;

    /**
     * 占用ID（预约ID、课程安排ID或签到活动ID）
     */
    private String occupationId;

    /**
     * 占用者类型（教师、学生）
     */
    private String occupierType;

    /**
     * 占用者ID
     */
    private String occupierId;

    /**
     * 占用者姓名
     */
    private String occupierName;

    /**
     * 课程名称（如果是课程安排或签到活动）
     */
    private String courseName;

    /**
     * 预约用途（如果是预约）
     */
    private String purpose;

    /**
     * 占用日期
     */
    private LocalDate date;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 状态（预约状态、签到活动状态等）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 占用类型枚举
     */
    public enum OccupationType {
        RESERVATION("预约"),
        COURSE_SCHEDULE("课程安排"),
        ATTENDANCE_SESSION("签到活动");

        private final String description;

        OccupationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

