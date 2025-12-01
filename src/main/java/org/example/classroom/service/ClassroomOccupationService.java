package org.example.classroom.service;

import org.example.classroom.dto.ClassroomConflictResult;
import org.example.classroom.dto.ClassroomOccupationInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 教室占用冲突检测服务
 * 统一检查预约、课程安排、签到活动等多种占用方式的冲突
 */
public interface ClassroomOccupationService {

    /**
     * 检查教室在指定时间段是否被占用（统一检测）
     * @param classroomId 教室ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeReservationId 排除的预约ID（用于更新时排除自身）
     * @param excludeScheduleId 排除的课程安排ID（用于更新时排除自身）
     * @return 冲突检测结果
     */
    ClassroomConflictResult checkClassroomOccupation(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String excludeReservationId,
            String excludeScheduleId
    );

    /**
     * 检查教室是否被占用（简化版本，不排除任何记录）
     */
    default ClassroomConflictResult checkClassroomOccupation(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return checkClassroomOccupation(classroomId, date, startTime, endTime, null, null);
    }

    /**
     * 获取教室在指定时间段的所有占用信息
     * @param classroomId 教室ID
     * @param date 日期
     * @return 占用信息列表
     */
    List<ClassroomOccupationInfo> getClassroomOccupations(String classroomId, LocalDate date);

    /**
     * 获取教室在指定日期和时间段内的占用信息
     */
    List<ClassroomOccupationInfo> getClassroomOccupationsInTimeRange(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
}

