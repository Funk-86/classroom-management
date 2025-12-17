package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_classes")
public class CourseClass {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String courseId;
    private String classId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String classCode;
}

