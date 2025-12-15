package org.example.classroom.service.impl;

import org.example.classroom.dto.ClassroomConflictResult;
import org.example.classroom.dto.ClassroomOccupationInfo;
import org.example.classroom.entity.AttendanceSession;
import org.example.classroom.entity.CourseSchedule;
import org.example.classroom.entity.Reservation;
import org.example.classroom.entity.Course;
import org.example.classroom.entity.AttendanceRecord;
import org.example.classroom.mapper.AttendanceSessionMapper;
import org.example.classroom.mapper.AttendanceRecordMapper;
import org.example.classroom.mapper.CourseMapper;
import org.example.classroom.mapper.CourseScheduleMapper;
import org.example.classroom.mapper.ReservationMapper;
import org.example.classroom.service.ClassroomOccupationService;
import org.example.classroom.dto.ClassroomRealtimeResponse;
import org.example.classroom.dto.ClassroomRealtimeResponse.AttendanceSummary;
import org.example.classroom.dto.ClassroomRealtimeResponse.CurrentCourse;
import org.example.classroom.dto.ClassroomRealtimeResponse.CurrentReservation;
import org.example.classroom.dto.ClassroomRealtimeResponse.StudentLite;
import org.example.classroom.util.WeekCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassroomOccupationServiceImpl implements ClassroomOccupationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private CourseScheduleMapper courseScheduleMapper;

    @Autowired
    private AttendanceSessionMapper attendanceSessionMapper;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public ClassroomConflictResult checkClassroomOccupation(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String excludeReservationId,
            String excludeScheduleId
    ) {
        List<ClassroomOccupationInfo> allConflicts = new ArrayList<>();

        // 1. 检查预约冲突
        List<ClassroomOccupationInfo> reservationConflicts = checkReservationConflicts(
                classroomId, date, startTime, endTime, excludeReservationId
        );
        allConflicts.addAll(reservationConflicts);

        // 2. 检查课程安排冲突
        List<ClassroomOccupationInfo> scheduleConflicts = checkScheduleConflicts(
                classroomId, date, startTime, endTime, excludeScheduleId
        );
        allConflicts.addAll(scheduleConflicts);

        // 3. 检查签到活动冲突
        List<ClassroomOccupationInfo> attendanceConflicts = checkAttendanceSessionConflicts(
                classroomId, date, startTime, endTime
        );
        allConflicts.addAll(attendanceConflicts);

        // 汇总冲突结果
        if (allConflicts.isEmpty()) {
            return ClassroomConflictResult.noConflict();
        }

        // 确定冲突类型
        ClassroomConflictResult.ConflictType conflictType = determineConflictType(allConflicts);
        String message = buildConflictMessage(allConflicts);

        return ClassroomConflictResult.conflict(conflictType, allConflicts, message);
    }

    @Override
    public List<ClassroomOccupationInfo> getClassroomOccupations(String classroomId, LocalDate date) {
        List<ClassroomOccupationInfo> occupations = new ArrayList<>();

        // 获取预约占用
        List<Reservation> reservations = reservationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reservation>()
                        .eq("classroom_id", classroomId)
                        .eq("date", date)
                        .in("status", 0, 1) // 待审核和已通过
        );
        occupations.addAll(convertReservationsToOccupationInfo(reservations));

        // 获取课程安排占用
        Date sqlDate = Date.valueOf(date);
        List<CourseSchedule> schedules = courseScheduleMapper.checkClassroomConflict(
                classroomId, sqlDate, Time.valueOf(LocalTime.MIN), Time.valueOf(LocalTime.MAX)
        );
        occupations.addAll(convertSchedulesToOccupationInfo(schedules, date));

        // 获取签到活动占用
        List<AttendanceSession> sessions = attendanceSessionMapper.checkAttendanceSessionConflict(
                classroomId, sqlDate, Time.valueOf(LocalTime.MIN), Time.valueOf(LocalTime.MAX)
        );
        occupations.addAll(convertAttendanceSessionsToOccupationInfo(sessions));

        return occupations;
    }

    @Override
    public List<ClassroomOccupationInfo> getClassroomOccupationsInTimeRange(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        List<ClassroomOccupationInfo> allOccupations = getClassroomOccupations(classroomId, date);

        // 过滤出在指定时间段内的占用
        return allOccupations.stream()
                .filter(occupation -> isTimeOverlap(
                        occupation.getStartTime(),
                        occupation.getEndTime(),
                        startTime,
                        endTime
                ))
                .collect(Collectors.toList());
    }

    /**
     * 检查预约冲突
     */
    private List<ClassroomOccupationInfo> checkReservationConflicts(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String excludeReservationId
    ) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reservation> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("classroom_id", classroomId)
                .eq("date", date)
                .in("status", 0, 1) // 只检查待审核和已通过的预约
                .and(w -> w
                        .between("start_time", startTime, endTime.minusNanos(1))
                        .or()
                        .between("end_time", startTime.plusNanos(1), endTime)
                        .or()
                        .le("start_time", startTime)
                        .ge("end_time", endTime)
                );

        if (excludeReservationId != null) {
            wrapper.ne("reservation_id", excludeReservationId);
        }

        List<Reservation> conflicts = reservationMapper.selectList(wrapper);
        return convertReservationsToOccupationInfo(conflicts);
    }

    /**
     * 检查课程安排冲突
     */
    private List<ClassroomOccupationInfo> checkScheduleConflicts(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String excludeScheduleId
    ) {
        Date sqlDate = Date.valueOf(date);
        Time sqlStartTime = Time.valueOf(startTime);
        Time sqlEndTime = Time.valueOf(endTime);

        List<CourseSchedule> conflicts = courseScheduleMapper.checkClassroomConflict(
                classroomId, sqlDate, sqlStartTime, sqlEndTime
        );

        // 兼容每周重复课程：如果未查到冲突，再按周次/星期几检测
        if (conflicts.isEmpty()) {
            int weekNumber = WeekCalculator.getWeekNumber(date);
            WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);
            List<CourseSchedule> weekly = courseScheduleMapper.selectSchedulesByClassAndWeek(
                    null,
                    weekNumber,
                    weekRange.getStartDate(),
                    weekRange.getEndDate()
            );
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1-7
            conflicts = weekly.stream()
                    .filter(s -> classroomId.equals(s.getClassroomId()))
                    .filter(s -> (s.getDayOfWeek() != null && s.getDayOfWeek() == dayOfWeek)
                            && isTimeOverlap(s.getStartTime(), s.getEndTime(), startTime, endTime))
                    .collect(Collectors.toList());
        }

        // 排除自身
        if (excludeScheduleId != null) {
            conflicts.removeIf(schedule -> schedule.getScheduleId().equals(excludeScheduleId));
        }

        return convertSchedulesToOccupationInfo(conflicts, date);
    }

    /**
     * 时间重叠判断（含端点）
     */
    private boolean isTimeOverlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false;
        return !e1.isBefore(s2) && !e2.isBefore(s1);
    }

    /**
     * 检查签到活动冲突
     */
    private List<ClassroomOccupationInfo> checkAttendanceSessionConflicts(
            String classroomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        Date sqlDate = Date.valueOf(date);
        Time sqlStartTime = Time.valueOf(startTime);
        Time sqlEndTime = Time.valueOf(endTime);

        List<AttendanceSession> conflicts = attendanceSessionMapper.checkAttendanceSessionConflict(
                classroomId, sqlDate, sqlStartTime, sqlEndTime
        );

        return convertAttendanceSessionsToOccupationInfo(conflicts);
    }

    /**
     * 将预约转换为占用信息
     */
    private List<ClassroomOccupationInfo> convertReservationsToOccupationInfo(List<Reservation> reservations) {
        return reservations.stream().map(reservation -> {
            return ClassroomOccupationInfo.builder()
                    .occupationType(ClassroomOccupationInfo.OccupationType.RESERVATION)
                    .occupationId(reservation.getReservationId())
                    .occupierType("学生")
                    .occupierId(reservation.getUserId())
                    .occupierName(reservation.getStudentName())
                    .purpose(reservation.getPurpose())
                    .date(reservation.getDate())
                    .startTime(reservation.getStartTime())
                    .endTime(reservation.getEndTime())
                    .status(reservation.getStatus())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 将课程安排转换为占用信息
     */
    private List<ClassroomOccupationInfo> convertSchedulesToOccupationInfo(
            List<CourseSchedule> schedules,
            LocalDate date
    ) {
        return schedules.stream().map(schedule -> {
            return ClassroomOccupationInfo.builder()
                    .occupationType(ClassroomOccupationInfo.OccupationType.COURSE_SCHEDULE)
                    .occupationId(schedule.getScheduleId())
                    .occupierType("教师")
                    .occupierId(schedule.getTeacherId())
                    .occupierName(schedule.getTeacherName())
                    .courseName(schedule.getCourseName())
                    .date(schedule.getScheduleDate() != null ? schedule.getScheduleDate() : date)
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .createdAt(schedule.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 将签到活动转换为占用信息
     */
    private List<ClassroomOccupationInfo> convertAttendanceSessionsToOccupationInfo(
            List<AttendanceSession> sessions
    ) {
        return sessions.stream()
                .filter(session -> session.getClassroomId() != null)
                .map(session -> {
                    LocalDate sessionDate = session.getStartTime().toLocalDate();
                    LocalTime sessionStartTime = session.getStartTime().toLocalTime();
                    LocalTime sessionEndTime = session.getEndTime().toLocalTime();

                    return ClassroomOccupationInfo.builder()
                            .occupationType(ClassroomOccupationInfo.OccupationType.ATTENDANCE_SESSION)
                            .occupationId(session.getSessionId())
                            .occupierType("教师")
                            .occupierId(session.getTeacherId())
                            .occupierName(session.getTeacherName())
                            .courseName(session.getCourseName())
                            .date(sessionDate)
                            .startTime(sessionStartTime)
                            .endTime(sessionEndTime)
                            .status(session.getStatus())
                            .createdAt(session.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());
    }

    /**
     * 确定冲突类型
     */
    private ClassroomConflictResult.ConflictType determineConflictType(
            List<ClassroomOccupationInfo> conflicts
    ) {
        long reservationCount = conflicts.stream()
                .filter(c -> c.getOccupationType() == ClassroomOccupationInfo.OccupationType.RESERVATION)
                .count();
        long scheduleCount = conflicts.stream()
                .filter(c -> c.getOccupationType() == ClassroomOccupationInfo.OccupationType.COURSE_SCHEDULE)
                .count();
        long attendanceCount = conflicts.stream()
                .filter(c -> c.getOccupationType() == ClassroomOccupationInfo.OccupationType.ATTENDANCE_SESSION)
                .count();

        int typeCount = 0;
        if (reservationCount > 0) typeCount++;
        if (scheduleCount > 0) typeCount++;
        if (attendanceCount > 0) typeCount++;

        if (typeCount > 1) {
            return ClassroomConflictResult.ConflictType.MULTIPLE;
        } else if (reservationCount > 0) {
            return ClassroomConflictResult.ConflictType.RESERVATION;
        } else if (scheduleCount > 0) {
            return ClassroomConflictResult.ConflictType.COURSE_SCHEDULE;
        } else if (attendanceCount > 0) {
            return ClassroomConflictResult.ConflictType.ATTENDANCE_SESSION;
        }

        return ClassroomConflictResult.ConflictType.NONE;
    }

    /**
     * 构建冲突消息
     */
    private String buildConflictMessage(List<ClassroomOccupationInfo> conflicts) {
        if (conflicts.isEmpty()) {
            return "无冲突";
        }

        StringBuilder message = new StringBuilder("教室在该时间段已被占用：\n");
        for (ClassroomOccupationInfo conflict : conflicts) {
            String occupierName = conflict.getOccupierName();
            if (occupierName == null || occupierName.trim().isEmpty()) {
                // 如果占用者姓名为空，使用更友好的占位文本，避免出现 "null"
                occupierName = "未知";
            }
            message.append(String.format("- %s：%s (%s %s-%s)\n",
                    conflict.getOccupationType().getDescription(),
                    occupierName,
                    conflict.getDate(),
                    conflict.getStartTime(),
                    conflict.getEndTime()
            ));
        }

        return message.toString();
    }

    @Override
    public ClassroomRealtimeResponse getRealtimeStatus(String classroomId) {
        // 统一使用东八区时间，避免服务器默认时区导致占用时间判断不一致
        java.time.ZonedDateTime shanghaiNow = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        LocalDate today = shanghaiNow.toLocalDate();
        LocalTime now = shanghaiNow.toLocalTime();
        int currentWeek = WeekCalculator.getCurrentWeek();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1-7

        ClassroomRealtimeResponse response = new ClassroomRealtimeResponse();
        response.setClassroomId(classroomId);

        // 1) 当前课程安排
        CourseSchedule currentSchedule = courseScheduleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseSchedule>()
                        .eq("classroom_id", classroomId)
                        .and(w -> w
                                .and(w1 -> w1.eq("schedule_type", 0)
                                        .eq("day_of_week", dayOfWeek)
                                        .le("start_week", currentWeek)
                                        .ge("end_week", currentWeek))
                                .or(w2 -> w2.eq("schedule_type", 1)
                                        .eq("schedule_date", today))
                        )
                        .le("start_time", now)
                        .gt("end_time", now)
                        .last("LIMIT 1")
        );

        if (currentSchedule != null) {
            response.setActivityType("COURSE_SCHEDULE");
            CurrentCourse cc = new CurrentCourse();
            cc.setScheduleId(currentSchedule.getScheduleId());
            cc.setCourseId(currentSchedule.getCourseId());
            cc.setScheduleType(currentSchedule.getScheduleType());
            cc.setDayOfWeek(currentSchedule.getDayOfWeek());
            cc.setScheduleDate(currentSchedule.getScheduleDate());
            cc.setStartTime(currentSchedule.getStartTime());
            cc.setEndTime(currentSchedule.getEndTime());
            cc.setStartWeek(currentSchedule.getStartWeek());
            cc.setEndWeek(currentSchedule.getEndWeek());

            if (currentSchedule.getCourseId() != null) {
                Course course = courseMapper.selectCourseWithDetail(currentSchedule.getCourseId());
                if (course != null) {
                    cc.setCourseName(course.getCourseName());
                    cc.setTeacherId(course.getTeacherId());
                    cc.setTeacherName(course.getTeacherName());
                }
            }
            response.setCurrentCourse(cc);
        }

        // 2) 若无课程，尝试预约占用
        if (response.getActivityType() == null) {
            Reservation reservation = reservationMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reservation>()
                            .eq("classroom_id", classroomId)
                            .eq("date", today)
                            .eq("status", 1)
                            .le("start_time", now)
                            .gt("end_time", now)
                            .last("LIMIT 1")
            );
            if (reservation != null) {
                response.setActivityType("RESERVATION");
                CurrentReservation cr = new CurrentReservation();
                cr.setReservationId(reservation.getReservationId());
                cr.setUserId(reservation.getUserId());
                cr.setUserName(reservation.getStudentName());
                cr.setPurpose(reservation.getPurpose());
                cr.setDate(reservation.getDate());
                cr.setStartTime(reservation.getStartTime());
                cr.setEndTime(reservation.getEndTime());
                response.setCurrentReservation(cr);
            } else {
                response.setActivityType("NONE");
            }
        }

        // 3) 当前签到活动（基于教室）
        AttendanceSession activeSession = attendanceSessionMapper.selectActiveSessionByClassroom(classroomId);
        if (activeSession != null) {
            AttendanceSummary summary = new AttendanceSummary();
            summary.setSessionId(activeSession.getSessionId());
            summary.setSessionTitle(activeSession.getSessionTitle());

            List<AttendanceRecord> records = attendanceRecordMapper.selectRecordsBySession(activeSession.getSessionId());
            int checkinCount = (int) records.stream()
                    .filter(r -> r.getCheckinStatus() != null && r.getCheckinStatus() == 1)
                    .count();
            List<StudentLite> absent = records.stream()
                    .filter(r -> r.getCheckinStatus() == null || r.getCheckinStatus() != 1)
                    .map(r -> {
                        StudentLite lite = new StudentLite();
                        lite.setStudentId(r.getStudentId());
                        lite.setStudentName(r.getStudentName());
                        return lite;
                    })
                    .collect(Collectors.toList());

            summary.setTotalStudents(records.size());
            summary.setCheckinCount(checkinCount);
            summary.setAbsentCount(absent.size());
            summary.setAbsentStudents(absent);
            response.setAttendance(summary);
        }

        // 状态文本
        if ("COURSE_SCHEDULE".equals(response.getActivityType()) || "RESERVATION".equals(response.getActivityType())) {
            response.setStatus(1);
            response.setStatusText("占用中");
        } else {
            response.setStatus(0);
            response.setStatusText("空闲");
        }

        return response;
    }
}

