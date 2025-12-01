package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("attendance_sessions")
public class AttendanceSession {
    @TableId(type = IdType.ASSIGN_ID)
    private String sessionId;

    private String courseId;
    private String courseScheduleId;
    private String teacherId;
    private String classroomId;
    private String sessionTitle;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer radius; // 允许签到范围（米）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer status; // 0已结束,1进行中,2已取消

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String classroomName;

    @TableField(exist = false)
    private Integer totalStudents; // 应签到的学生总数

    @TableField(exist = false)
    private Integer checkedInCount; // 已签到人数
}

