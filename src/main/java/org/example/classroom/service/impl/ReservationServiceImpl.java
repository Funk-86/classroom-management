package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.dto.ApprovalRequest;
import org.example.classroom.dto.UserReservationHistoryResponse;
import org.example.classroom.entity.Reservation;
import org.example.classroom.mapper.ReservationMapper;
import org.example.classroom.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @org.springframework.beans.factory.annotation.Autowired
    private org.example.classroom.service.ClassroomOccupationService classroomOccupationService;

    @Override
    public IPage<Reservation> getAllReservations(Integer page, Integer size, Integer status,
                                                 LocalDate startDate, LocalDate endDate) {
        Page<Reservation> pageParam = new Page<>(page, size);

        try {
            // 使用修复后的JOIN查询方法
            return baseMapper.selectAllReservationsWithDetails(pageParam, status, startDate, endDate);
        } catch (Exception e) {
            // 如果JOIN查询失败，回退到基本查询
            log.warn("JOIN查询失败，使用基本查询: {}", e.getMessage());

            QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();

            // 只选择reservations表中实际存在的字段
            queryWrapper.select(
                    "reservation_id", "classroom_id", "user_id", "purpose",
                    "date", "start_time", "end_time", "status", "admin_notes",
                    "created_at", "updated_at"
            );

            if (status != null) {
                queryWrapper.eq("status", status);
            }

            if (startDate != null) {
                queryWrapper.ge("date", startDate);
            }

            if (endDate != null) {
                queryWrapper.le("date", endDate);
            }

            queryWrapper.orderByDesc("date").orderByDesc("start_time");

            return baseMapper.selectPage(pageParam, queryWrapper);
        }
    }

    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // 使用统一的教室占用冲突检测
        org.example.classroom.dto.ClassroomConflictResult conflictResult =
                classroomOccupationService.checkClassroomOccupation(
                        reservation.getClassroomId(),
                        reservation.getDate(),
                        reservation.getStartTime(),
                        reservation.getEndTime(),
                        null, // 新建预约，无需排除
                        null
                );

        if (conflictResult.isHasConflict()) {
            throw new IllegalArgumentException("该时间段已被占用: " + conflictResult.getMessage());
        }

        // 设置默认状态为待审核
        reservation.setStatus(0);
        save(reservation);
        return reservation;
    }

    @Override
    public Reservation getReservationById(String id) {
        try {
            // 优先使用JOIN查询获取完整信息
            return baseMapper.selectReservationWithDetailsById(id);
        } catch (Exception e) {
            // 备用方案：基本查询
            log.warn("JOIN查询失败，使用基本查询: {}", e.getMessage());
            Reservation reservation = getById(id);
            if (reservation != null) {
                // 可以在这里补充一些默认信息
                reservation.setClassroomName("未知教室");
                reservation.setBuildingName("未知教学楼");
                reservation.setStudentName("未知用户");
            }
            return reservation;
        }
    }

    @Override
    public List<Reservation> getUserReservations(String userId, Integer page, Integer size) {
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("date")
                .orderByDesc("start_time");

        // 手动实现分页
        if (page != null && size != null) {
            queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size);
        }

        return list(queryWrapper);
    }

    @Override
    public List<Reservation> getClassroomReservations(String classroomId, LocalDate date, Integer page, Integer size) {
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classroom_id", classroomId)
                .eq("date", date)
                .orderByAsc("start_time");

        // 手动实现分页
        if (page != null && size != null) {
            queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size);
        }

        return list(queryWrapper);
    }

    @Override
    public boolean cancelReservation(String id) {
        Reservation reservation = getById(id);
        if (reservation != null) {
            reservation.setStatus(3); // 3表示已取消
            return updateById(reservation);
        }
        return false;
    }

    @Override
    public boolean checkTimeConflict(String classroomId, LocalDate date, String startTime, String endTime) {
        LocalTime newStart = LocalTime.parse(startTime);
        LocalTime newEnd = LocalTime.parse(endTime);

        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classroom_id", classroomId)
                .eq("date", date)
                .in("status", 0, 1) // 只检查待审核和已通过的预约
                .and(wrapper -> wrapper
                        .between("start_time", newStart, newEnd.minusNanos(1))
                        .or()
                        .between("end_time", newStart.plusNanos(1), newEnd)
                        .or()
                        .le("start_time", newStart)
                        .ge("end_time", newEnd));

        return count(queryWrapper) > 0;
    }

    @Override
    public IPage<UserReservationHistoryResponse> getUserHistoryReservations(String userId,
                                                                            Integer page,
                                                                            Integer size) {
        Page<Reservation> pageParam = new Page<>(page, size);
        IPage<Reservation> reservationPage = baseMapper.selectUserHistoryReservations(pageParam, userId);

        return convertToResponsePage(reservationPage);
    }

    @Override
    public IPage<UserReservationHistoryResponse> getUserHistoryReservationsByDateRange(String userId,
                                                                                       LocalDate startDate,
                                                                                       LocalDate endDate,
                                                                                       Integer page,
                                                                                       Integer size) {
        Page<Reservation> pageParam = new Page<>(page, size);
        IPage<Reservation> reservationPage = baseMapper.selectUserHistoryReservationsByDateRange(
                pageParam, userId, startDate, endDate);

        return convertToResponsePage(reservationPage);
    }

    @Override
    public long countByStatus(int status) {
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return count(queryWrapper);
    }

    @Override
    public List<Reservation> getRecentReservations(int limit) {
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at")
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public List<Reservation> getRecentReservationsByStatus(Integer status, Integer limit) {
        try {
            return baseMapper.selectRecentReservationsByStatus(status, limit);
        } catch (Exception e) {
            // 如果自定义查询失败，回退到基本查询
            log.warn("自定义查询失败，使用基本查询: {}", e.getMessage());

            QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", status)
                    .orderByDesc("created_at")
                    .last("LIMIT " + limit);
            return list(queryWrapper);
        }
    }

    private IPage<UserReservationHistoryResponse> convertToResponsePage(IPage<Reservation> reservationPage) {
        Page<UserReservationHistoryResponse> responsePage = new Page<>(
                reservationPage.getCurrent(),
                reservationPage.getSize(),
                reservationPage.getTotal()
        );

        List<UserReservationHistoryResponse> responseList = reservationPage.getRecords().stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());

        responsePage.setRecords(responseList);
        return responsePage;
    }

    private UserReservationHistoryResponse convertToHistoryResponse(Reservation reservation) {
        UserReservationHistoryResponse response = new UserReservationHistoryResponse();
        BeanUtils.copyProperties(reservation, response);
        return response;
    }

    @Override
    @Transactional
    public boolean approveReservation(ApprovalRequest approvalRequest, String approverId) {
        // 验证预约是否存在
        Reservation reservation = getById(approvalRequest.getReservationId());
        if (reservation == null) {
            throw new IllegalArgumentException("预约不存在");
        }

        // 验证预约状态是否为待审核
        if (reservation.getStatus() != 0) {
            throw new IllegalArgumentException("只能审批待审核的预约");
        }

        // 更新预约状态
        reservation.setStatus(approvalRequest.getAction()); // 1:通过, 2:拒绝
        reservation.setApproverId(approverId);
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        // 如果是拒绝，设置拒绝原因
        if (approvalRequest.getAction() == 2) {
            reservation.setRejectReason(approvalRequest.getNotes());
        } else {
            reservation.setAdminNotes(approvalRequest.getNotes());
        }

        return updateById(reservation);
    }

    @Override
    public IPage<Reservation> getPendingApprovals(Integer page, Integer size,
                                                  LocalDate startDate, LocalDate endDate) {
        Page<Reservation> pageParam = new Page<>(page, size);

        try {
            // 修复：添加缺失的参数
            return baseMapper.selectPendingApprovalsWithDetails(pageParam, startDate, endDate);
        } catch (Exception e) {
            // 回退到基本查询
            log.warn("JOIN查询失败，使用基本查询: {}", e.getMessage());

            QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 0) // 待审核状态
                    .orderByAsc("date")
                    .orderByAsc("start_time");

            if (startDate != null) {
                queryWrapper.ge("date", startDate);
            }
            if (endDate != null) {
                queryWrapper.le("date", endDate);
            }

            return baseMapper.selectPage(pageParam, queryWrapper);
        }
    }

    @Override
    public List<Reservation> getTodayPendingApprovals() {
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0) // 待审核状态
                .eq("date", LocalDate.now())
                .orderByAsc("start_time");

        return list(queryWrapper);
    }

    @Override
    public IPage<Reservation> getApprovalHistory(String approverId, Integer page, Integer size,
                                                 LocalDate startDate, LocalDate endDate) {
        Page<Reservation> pageParam = new Page<>(page, size);

        try {
            return baseMapper.selectApprovalHistoryWithDetails(pageParam, approverId, startDate, endDate);
        } catch (Exception e) {
            // 回退到基本查询
            log.warn("JOIN查询失败，使用基本查询: {}", e.getMessage());

            QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("approver_id", approverId)
                    .in("status", 1, 2) // 已通过或已拒绝
                    .orderByDesc("approve_time");

            if (startDate != null) {
                queryWrapper.ge("date", startDate);
            }
            if (endDate != null) {
                queryWrapper.le("date", endDate);
            }

            return baseMapper.selectPage(pageParam, queryWrapper);
        }
    }
}