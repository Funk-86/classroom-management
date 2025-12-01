package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.Reservation;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    // 1. 管理后台查询所有预约（分页）- 添加用户表关联
    @Select("<script>" +
            "SELECT " +
            "r.reservation_id, " +
            "r.classroom_id, " +
            "r.user_id, " +
            "r.purpose, " +
            "r.date, " +
            "r.start_time, " +
            "r.end_time, " +
            "r.status, " +
            "r.admin_notes, " +
            "r.created_at, " +
            "r.updated_at, " +
            "c.classroom_name, " +
            "b.building_name, " +
            "u.user_name as student_name " +  // 添加申请人姓名
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +  // 新增：关联用户表
            "WHERE 1=1 " +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='startDate != null'> AND r.date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.date &lt;= #{endDate} </if>" +
            "ORDER BY r.date DESC, r.start_time DESC" +
            "</script>")
    IPage<Reservation> selectAllReservationsWithDetails(Page<Reservation> page,
                                                        @Param("status") Integer status,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // 2. 管理后台查询待审核预约（用于最近预约）- 专门的方法
    @Select("SELECT " +
            "r.reservation_id, " +
            "r.classroom_id, " +
            "r.user_id, " +
            "r.purpose, " +
            "r.date, " +
            "r.start_time, " +
            "r.end_time, " +
            "r.status, " +
            "r.admin_notes, " +
            "r.created_at, " +
            "r.updated_at, " +
            "c.classroom_name, " +
            "b.building_name, " +
            "u.user_name as student_name " +  // 添加申请人姓名
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +  // 关联用户表获取姓名
            "WHERE r.status = #{status} " +
            "ORDER BY r.created_at DESC " +
            "LIMIT #{limit}")
    List<Reservation> selectRecentReservationsByStatus(@Param("status") Integer status,
                                                       @Param("limit") Integer limit);

    // 3. 用户历史预约记录（包含教室和教学楼信息）- 您的原始方法
    @Select("SELECT r.reservation_id, r.classroom_id, r.user_id, r.purpose, " +
            "r.date, r.start_time, r.end_time, r.status, r.admin_notes, " +
            "r.created_at, r.updated_at, " +
            "c.classroom_name, b.building_name " +
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.date DESC, r.start_time DESC")
    IPage<Reservation> selectUserHistoryReservations(Page<Reservation> page,
                                                     @Param("userId") String userId);

    // 4. 用户特定时间段的历史预约记录
    @Select("<script>" +
            "SELECT r.reservation_id, r.classroom_id, r.user_id, r.purpose, " +
            "r.date, r.start_time, r.end_time, r.status, r.admin_notes, " +
            "r.created_at, r.updated_at, " +
            "c.classroom_name, b.building_name " +
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "WHERE r.user_id = #{userId} " +
            "<if test='startDate != null'> AND r.date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.date &lt;= #{endDate} </if>" +
            "ORDER BY r.date DESC, r.start_time DESC" +
            "</script>")
    IPage<Reservation> selectUserHistoryReservationsByDateRange(Page<Reservation> page,
                                                                @Param("userId") String userId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    @Select("SELECT " +
            "r.reservation_id, r.classroom_id, r.user_id, r.purpose, " +
            "r.date, r.start_time, r.end_time, r.status, r.admin_notes, " +
            "r.created_at, r.updated_at, " +
            "c.classroom_name, b.building_name, u.user_name as student_name " +
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.reservation_id = #{id}")
    Reservation selectReservationWithDetailsById(@Param("id") String id);

    // 1. 获取待审批预约列表（包含详细信息）
    @Select("<script>" +
            "SELECT " +
            "r.reservation_id, r.classroom_id, r.user_id, r.purpose, " +
            "r.date, r.start_time, r.end_time, r.status, r.admin_notes, " +
            "r.created_at, r.updated_at, r.approver_id, r.approve_time, r.reject_reason, " +
            "c.classroom_name, b.building_name, u.user_name as student_name, " +
            "au.user_name as approver_name " +
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "LEFT JOIN users au ON r.approver_id = au.user_id " +
            "WHERE r.status = 0 " + // 待审核状态
            "<if test='startDate != null'> AND r.date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.date &lt;= #{endDate} </if>" +
            "ORDER BY r.date ASC, r.start_time ASC" +
            "</script>")
    IPage<Reservation> selectPendingApprovalsWithDetails(Page<Reservation> page,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    // 2. 获取审批历史记录
    @Select("<script>" +
            "SELECT " +
            "r.reservation_id, r.classroom_id, r.user_id, r.purpose, " +
            "r.date, r.start_time, r.end_time, r.status, r.admin_notes, " +
            "r.created_at, r.updated_at, r.approver_id, r.approve_time, r.reject_reason, " +
            "c.classroom_name, b.building_name, u.user_name as student_name, " +
            "au.user_name as approver_name " +
            "FROM reservations r " +
            "LEFT JOIN classrooms c ON r.classroom_id = c.classroom_id " +
            "LEFT JOIN buildings b ON c.building_id = b.building_id " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "LEFT JOIN users au ON r.approver_id = au.user_id " +
            "WHERE r.approver_id = #{approverId} " +
            "AND r.status IN (1, 2) " + // 已通过或已拒绝
            "<if test='startDate != null'> AND r.date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.date &lt;= #{endDate} </if>" +
            "ORDER BY r.approve_time DESC" +
            "</script>")
    IPage<Reservation> selectApprovalHistoryWithDetails(Page<Reservation> page,
                                                        @Param("approverId") String approverId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}