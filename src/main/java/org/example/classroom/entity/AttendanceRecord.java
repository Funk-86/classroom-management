package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("attendance_records")
public class AttendanceRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private String recordId;

    private String sessionId;
    private String studentId;

    private LocalDateTime checkinTime;
    private BigDecimal latitude; // 学生签到时的纬度
    private BigDecimal longitude; // 学生签到时的经度
    private Integer distance; // 距离签到点的距离（米）
    private Integer checkinStatus; // 1成功,2距离过远,3超时
    private String remark;

    // 状态字段（从checkinStatus转换而来，用于业务逻辑）
    @TableField(exist = false)
    private Integer status; // 签到状态(0正常,1迟到,2缺勤) - 不在数据库表中

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 关联字段
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String sessionTitle;
}

