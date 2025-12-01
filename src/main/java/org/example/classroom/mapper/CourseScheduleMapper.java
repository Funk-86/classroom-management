package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.CourseSchedule;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {

    @Select("SELECT cs.*, c.course_name, c.course_code, c.course_type, c.credit_hours, " +
            "c.teacher_id as teacher_id, " +
            "cl.classroom_name, b.building_name, " +
            "camp.campus_name, u.user_name as teacher_name " +
            "FROM course_schedules cs " +
            "LEFT JOIN courses c ON cs.course_id = c.course_id " +
            "LEFT JOIN classrooms cl ON cs.classroom_id = cl.classroom_id " +
            "LEFT JOIN buildings b ON cl.building_id = b.building_id " +
            "LEFT JOIN campuses camp ON cs.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "WHERE cs.schedule_id = #{scheduleId}")
    CourseSchedule selectScheduleWithDetail(String scheduleId);

    @Select("<script>" +
            "SELECT cs.*, c.course_name, c.course_code, c.course_type, c.credit_hours, " +
            "cl.classroom_name, b.building_name, " +
            "camp.campus_name, u.user_name as teacher_name " +
            "FROM course_schedules cs " +
            "LEFT JOIN courses c ON cs.course_id = c.course_id " +
            "LEFT JOIN classrooms cl ON cs.classroom_id = cl.classroom_id " +
            "LEFT JOIN buildings b ON cl.building_id = b.building_id " +
            "LEFT JOIN campuses camp ON cs.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='courseId != null and courseId != \"\"'> AND cs.course_id = #{courseId} </if>" +
            "<if test='classroomId != null and classroomId != \"\"'> AND cs.classroom_id = #{classroomId} </if>" +
            "<if test='campusId != null and campusId != \"\"'> AND cs.campus_id = #{campusId} </if>" +
            "<if test='dayOfWeek != null'> AND cs.day_of_week = #{dayOfWeek} </if>" +
            "<if test='scheduleType != null'> AND cs.schedule_type = #{scheduleType} </if>" +
            "<if test='startDate != null'> AND cs.schedule_date >= #{startDate} </if>" +
            "<if test='endDate != null'> AND cs.schedule_date &lt;= #{endDate} </if>" +
            "ORDER BY cs.day_of_week, cs.start_time, cs.schedule_date" +
            "</script>")
    IPage<CourseSchedule> selectSchedulesWithDetail(Page<CourseSchedule> page,
                                                    @Param("courseId") String courseId,
                                                    @Param("classroomId") String classroomId,
                                                    @Param("campusId") String campusId,
                                                    @Param("dayOfWeek") Integer dayOfWeek,
                                                    @Param("scheduleType") Integer scheduleType,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Select({
            "SELECT cs.*, c.course_name, c.course_code, c.course_type, c.credit_hours, ",
            "c.teacher_id, ",
            "cl.classroom_name, b.building_name, ",
            "camp.campus_name, u.user_name as teacher_name ",
            "FROM course_schedules cs ",
            "LEFT JOIN courses c ON cs.course_id = c.course_id ",
            "LEFT JOIN classrooms cl ON cs.classroom_id = cl.classroom_id ",
            "LEFT JOIN buildings b ON cl.building_id = b.building_id ",
            "LEFT JOIN campuses camp ON cs.campus_id = camp.campus_id ",
            "LEFT JOIN users u ON c.teacher_id = u.user_id ",
            "WHERE cs.classroom_id = #{classroomId} ",
            "AND ((cs.schedule_type = 0 ",
            "AND #{date} BETWEEN CONCAT(c.academic_year, '-09-01') AND CONCAT(SUBSTRING(c.academic_year, 6), '-02-28')) ",
            "OR (cs.schedule_type = 1 AND cs.schedule_date = #{date})) ",
            "AND cs.start_time <= #{endTime} ",
            "AND cs.end_time > #{startTime}"
    })
    List<CourseSchedule> checkClassroomConflict(
            @Param("classroomId") String classroomId,
            @Param("date") Date date,
            @Param("startTime") Time startTime,
            @Param("endTime") Time endTime
    );

    @Select("<script>" +
            "SELECT cs.*, c.course_name, c.course_code, c.course_type, c.credit_hours, " +
            "cl.classroom_name, b.building_name, " +
            "camp.campus_name, u.user_name as teacher_name " +
            "FROM course_schedules cs " +
            "LEFT JOIN courses c ON cs.course_id = c.course_id " +
            "LEFT JOIN classrooms cl ON cs.classroom_id = cl.classroom_id " +
            "LEFT JOIN buildings b ON cl.building_id = b.building_id " +
            "LEFT JOIN campuses camp ON cs.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='classId != null and classId != \"\"'> " +
            "AND c.class_id = #{classId} " +
            "</if>" +
            "<if test='weekNumber != null'> " +
            "AND ((cs.schedule_type = 0 AND #{weekNumber} BETWEEN cs.start_week AND cs.end_week) " +
            "OR (cs.schedule_type = 1 AND cs.schedule_date BETWEEN #{startDate} AND #{endDate})) " +
            "</if>" +
            "ORDER BY cs.day_of_week, cs.start_time" +
            "</script>")
    List<CourseSchedule> selectSchedulesByClassAndWeek(@Param("classId") String classId,
                                                       @Param("weekNumber") Integer weekNumber,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}