package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("semesters")
public class Semester {

    @TableId(type = IdType.ASSIGN_ID)
    private String semesterId;

    private String name; // 春季/秋季
    private String academicYear; // 2025-2026
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer weeks;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getWeeks() { return weeks; }
    public void setWeeks(Integer weeks) { this.weeks = weeks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

