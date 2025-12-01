package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.AttendanceRecord;

import java.util.List;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    @Select("SELECT " +
            "COALESCE(r.record_id, CONCAT('temp_', all_students.student_id)) as record_id, " +
            "#{sessionId} as session_id, " +
            "all_students.student_id, " +
            "all_students.student_name, " +
            "r.checkin_time, " +
            "r.latitude, " +
            "r.longitude, " +
            "r.distance, " +
            "r.checkin_status, " +
            "r.remark, " +
            "c.course_name, " +
            "s.session_title, " +
            "CASE " +
            "  WHEN r.checkin_status = 1 THEN 0 " +
            "  WHEN r.checkin_status = 2 THEN 2 " +
            "  WHEN r.checkin_status = 3 THEN 2 " +
            "  ELSE 2 " +
            "END as status " +
            "FROM attendance_sessions s " +
            "JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN ( " +
            "  SELECT DISTINCT sc.student_id, u.user_name as student_name " +
            "  FROM student_courses sc " +
            "  JOIN users u ON sc.student_id = u.user_id AND u.user_role = 0 " +
            "  WHERE sc.course_id = (SELECT course_id FROM attendance_sessions WHERE session_id = #{sessionId}) " +
            "    AND sc.enrollment_status = 1 " +
            "  UNION " +
            "  SELECT DISTINCT u2.user_id as student_id, u2.user_name as student_name " +
            "  FROM courses c_inner " +
            "  JOIN users u2 ON c_inner.class_id = u2.class_id AND u2.user_role = 0 " +
            "  WHERE c_inner.course_id = (SELECT course_id FROM attendance_sessions WHERE session_id = #{sessionId}) " +
            "    AND c_inner.class_id IS NOT NULL " +
            ") all_students ON 1=1 " +
            "LEFT JOIN attendance_records r ON r.session_id = #{sessionId} " +
            "  AND r.student_id = all_students.student_id " +
            "WHERE s.session_id = #{sessionId} " +
            "ORDER BY r.checkin_time DESC, all_students.student_name")
    List<AttendanceRecord> selectRecordsBySession(@Param("sessionId") String sessionId);

    @Select("SELECT r.*, c.course_name, s.session_title " +
            "FROM attendance_records r " +
            "LEFT JOIN attendance_sessions s ON r.session_id = s.session_id " +
            "LEFT JOIN courses c ON s.course_id = c.course_id " +
            "WHERE r.student_id = #{studentId} " +
            "ORDER BY r.checkin_time DESC " +
            "LIMIT #{limit}")
    List<AttendanceRecord> selectStudentRecords(@Param("studentId") String studentId, @Param("limit") Integer limit);
}

