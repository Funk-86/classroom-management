package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.Classroom;

import java.time.LocalDate;
import java.time.LocalTime;

@Mapper
public interface ClassroomMapper extends BaseMapper<Classroom> {

    @Select("<script>" +
            "SELECT c.* FROM classrooms c WHERE c.status = 0 " +
            "AND c.capacity >= #{minCapacity} " +
            "AND c.classroom_id NOT IN (" +
            "   SELECT r.classroom_id FROM reservations r " +
            "   WHERE r.date = #{date} " +
            "   AND ((r.start_time &lt;= #{endTime} AND r.end_time &gt; #{startTime}) " +
            "   OR (r.start_time &gt;= #{startTime} AND r.start_time &lt; #{endTime})) " +
            "   AND r.status = 1" +
            ")" +
            "<if test='buildingId != null'> AND c.building_id = #{buildingId}</if>" +
            "<if test='equipment != null'> AND c.equipment LIKE CONCAT('%', #{equipment}, '%')</if>" +
            "ORDER BY c.building_id, c.floor_num, c.classroom_name" +
            "</script>")
    IPage<Classroom> selectAvailableClassrooms(Page<Classroom> page,
                                               @Param("date") LocalDate date,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime,
                                               @Param("minCapacity") Integer minCapacity,
                                               @Param("buildingId") String buildingId,
                                               @Param("equipment") String equipment);

    // 检查教室是否有关联的预约记录
    @Select("SELECT COUNT(*) FROM reservations WHERE classroom_id = #{classroomId}")
    long countReservationsByClassroomId(@Param("classroomId") String classroomId);

    // 检查教室是否有关联的课程安排（只检查有效的课程安排，course_id不为空且对应的课程存在）
    @Select("SELECT COUNT(*) FROM course_schedules cs " +
            "INNER JOIN courses c ON cs.course_id = c.course_id " +
            "WHERE cs.classroom_id = #{classroomId} AND cs.course_id IS NOT NULL")
    long countCourseSchedulesByClassroomId(@Param("classroomId") String classroomId);

    // 检查教室是否有关联的旧课程表记录（schedules表）
    @Select("SELECT COUNT(*) FROM schedules WHERE classroom_id = #{classroomId}")
    long countSchedulesByClassroomId(@Param("classroomId") String classroomId);

    // 检查教室是否有关联的教室状态记录
    @Select("SELECT COUNT(*) FROM classroom_status WHERE classroom_id = #{classroomId}")
    long countClassroomStatusByClassroomId(@Param("classroomId") String classroomId);
}