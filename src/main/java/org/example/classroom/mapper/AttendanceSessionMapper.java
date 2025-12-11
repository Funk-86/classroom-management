package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.AttendanceSession;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AttendanceSessionMapper extends BaseMapper<AttendanceSession> {

    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, " +
            "cl.classroom_name " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.session_id = #{sessionId}")
    AttendanceSession selectSessionWithDetail(@Param("sessionId") String sessionId);

    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, " +
            "cl.classroom_name, " +
            "(SELECT COUNT(*) FROM attendance_records WHERE session_id = s.session_id) as checked_in_count " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.teacher_id = #{teacherId} " +
            "ORDER BY s.start_time DESC " +
            "LIMIT #{limit}")
    List<AttendanceSession> selectTeacherSessions(@Param("teacherId") String teacherId, @Param("limit") Integer limit);

    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, " +
            "cl.classroom_name " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.course_id = #{courseId} " +
            "AND s.status = 1 " +
            "AND NOW() BETWEEN s.start_time AND s.end_time " +
            "ORDER BY s.start_time DESC " +
            "LIMIT 1")
    AttendanceSession selectActiveSessionByCourse(@Param("courseId") String courseId);

    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, " +
            "cl.classroom_name " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.course_id = #{courseId} " +
            "AND s.status != 2 " +
            "ORDER BY s.start_time DESC")
    List<AttendanceSession> selectCourseSessions(@Param("courseId") String courseId);

    @Select("SELECT s.* FROM attendance_sessions s " +
            "WHERE s.status = 1 " +
            "AND NOW() BETWEEN s.start_time AND s.end_time " +
            "AND s.course_id IN (" +
            "  SELECT course_id FROM student_courses WHERE student_id = #{studentId} AND enrollment_status = 1" +
            ") " +
            "ORDER BY s.start_time DESC")
    List<AttendanceSession> selectActiveSessionsForStudent(@Param("studentId") String studentId);

    /**
     * 检查签到活动在指定时间段是否有冲突
     */
    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, " +
            "cl.classroom_name " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.classroom_id = #{classroomId} " +
            "AND s.classroom_id IS NOT NULL " +
            "AND s.status != 2 " + // 排除已取消的
            "AND DATE(s.start_time) = #{date} " +
            "AND TIME(s.start_time) < #{endTime} " +
            "AND TIME(s.end_time) > #{startTime}")
    List<AttendanceSession> checkAttendanceSessionConflict(
            @Param("classroomId") String classroomId,
            @Param("date") java.sql.Date date,
            @Param("startTime") java.sql.Time startTime,
            @Param("endTime") java.sql.Time endTime
    );

    /**
     * 查询指定教室正在进行的签到活动（用于扫码展示）
     */
    @Select("SELECT s.*, c.course_name, u.user_name as teacher_name, cl.classroom_name " +
            "FROM attendance_sessions s " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN users u ON s.teacher_id = u.user_id " +
            "LEFT JOIN classrooms cl ON s.classroom_id = cl.classroom_id " +
            "WHERE s.classroom_id = #{classroomId} " +
            "AND s.status = 1 " +
            "AND NOW() BETWEEN s.start_time AND s.end_time " +
            "ORDER BY s.start_time DESC " +
            "LIMIT 1")
    AttendanceSession selectActiveSessionByClassroom(@Param("classroomId") String classroomId);
}

