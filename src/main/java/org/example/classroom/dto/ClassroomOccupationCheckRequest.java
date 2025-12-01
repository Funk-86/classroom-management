package org.example.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 教室占用检查请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOccupationCheckRequest {
    @NotBlank(message = "教室ID不能为空")
    private String classroomId;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    /**
     * 排除的预约ID（用于更新时排除自身）
     */
    private String excludeReservationId;

    /**
     * 排除的课程安排ID（用于更新时排除自身）
     */
    private String excludeScheduleId;

    /**
     * 验证时间范围
     */
    public String validate() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            return "开始时间必须早于结束时间";
        }
        return null;
    }
}

