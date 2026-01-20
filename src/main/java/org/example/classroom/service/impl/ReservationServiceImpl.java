package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.dto.ApprovalRequest;
import org.example.classroom.dto.UserReservationHistoryResponse;
import org.example.classroom.entity.*;
import org.example.classroom.mapper.ReservationMapper;
import org.example.classroom.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @org.springframework.beans.factory.annotation.Autowired
    private org.example.classroom.service.ClassroomOccupationService classroomOccupationService;

    @org.springframework.beans.factory.annotation.Autowired
    private HolidayService holidayService;

    @org.springframework.beans.factory.annotation.Autowired
    private ClassroomService classroomService;

    @org.springframework.beans.factory.annotation.Autowired
    private BuildingService buildingService;

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;

    // 用于执行延迟任务的线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    // 统一使用北京时间（Asia/Shanghai, GMT+8）
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

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
        // 检查是否在假期内
        checkHolidayConflict(reservation.getClassroomId(), reservation.getDate());

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

        // 检查是否需要自动通过：教师预约且用途为"上课"或"会议"
        checkAndScheduleAutoApproval(reservation);

        return reservation;
    }

    /**
     * 检查预约是否需要自动通过，如果需要则安排1分钟后的自动审核
     * 条件：教师预约（user_role = 1）且用途为"上课"或"会议"
     */
    private void checkAndScheduleAutoApproval(Reservation reservation) {
        try {
            String reservationId = reservation.getReservationId();
            String userId = reservation.getUserId();
            String purpose = reservation.getPurpose();

            log.info("开始检查自动通过条件，预约ID: {}, 用户ID: {}, 用途: {}", reservationId, userId, purpose);

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("无法找到用户信息，预约ID: {}, 用户ID: {}", reservationId, userId);
                return;
            }

            // 检查是否为教师（user_role = 1）
            Integer userRole = user.getUserRole();
            log.info("用户角色检查，预约ID: {}, 用户ID: {}, 用户角色: {}", reservationId, userId, userRole);
            if (userRole == null || userRole != 1) {
                log.info("用户不是教师，不安排自动通过，预约ID: {}, 用户角色: {}", reservationId, userRole);
                return; // 不是教师，不需要自动通过
            }

            // 检查用途是否为"上课"或"会议"
            if (purpose == null) {
                log.warn("预约用途为空，不安排自动通过，预约ID: {}", reservationId);
                return;
            }

            purpose = purpose.trim();
            boolean isAutoApprovePurpose = purpose.equals("上课") || purpose.equals("会议");
            log.info("用途检查，预约ID: {}, 用途: [{}], 是否匹配: {}", reservationId, purpose, isAutoApprovePurpose);

            if (!isAutoApprovePurpose) {
                log.info("用途不是'上课'或'会议'，不安排自动通过，预约ID: {}, 用途: [{}]", reservationId, purpose);
                return; // 用途不是"上课"或"会议"，不需要自动通过
            }

            // 安排1分钟后的自动审核任务
            log.info("教师预约自动通过任务已安排，预约ID: {}, 用途: {}, 将在1分钟后自动审核", reservationId, purpose);

            scheduler.schedule(() -> {
                try {
                    autoApproveReservation(reservationId);
                } catch (Exception e) {
                    log.error("自动审核预约失败，预约ID: {}", reservationId, e);
                }
            }, 1, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.error("检查自动通过条件时发生错误，预约ID: {}", reservation.getReservationId(), e);
        }
    }

    /**
     * 自动通过预约（如果预约仍处于待审核状态）
     */
    private void autoApproveReservation(String reservationId) {
        try {
            Reservation reservation = getById(reservationId);
            if (reservation == null) {
                log.warn("预约不存在，无法自动通过，预约ID: {}", reservationId);
                return;
            }

            // 检查预约状态是否为待审核（status = 0）
            if (reservation.getStatus() != 0) {
                log.info("预约已被管理员处理，无需自动通过，预约ID: {}, 当前状态: {}", reservationId, reservation.getStatus());
                return;
            }

            // 自动通过预约
            reservation.setStatus(1); // 1表示已通过
            // 外键约束导致失败时，优先写 null（允许外键为空即可通过）
            reservation.setApproverId(null);
            reservation.setApproveTime(LocalDateTime.now(BEIJING_ZONE)); // 使用北京时间
            reservation.setUpdatedAt(LocalDateTime.now(BEIJING_ZONE)); // 使用北京时间
            // 自动填写审核备注为“通过”，并保留说明
            reservation.setAdminNotes("通过（系统自动通过，教师预约，用途：" + reservation.getPurpose() + "，1分钟内未审核）");

            boolean success = updateById(reservation);
            if (success) {
                log.info("预约已自动通过，预约ID: {}, 用途: {}", reservationId, reservation.getPurpose());
            } else {
                log.error("自动通过预约失败，预约ID: {}", reservationId);
            }
        } catch (Exception e) {
            log.error("自动通过预约时发生错误，预约ID: {}", reservationId, e);
        }
    }

    /**
     * 检查预约日期是否在假期内
     * @param classroomId 教室ID
     * @param date 预约日期
     * @throws IllegalArgumentException 如果日期在假期内
     */
    private void checkHolidayConflict(String classroomId, LocalDate date) {
        try {
            // 获取教室信息
            Classroom classroom = classroomService.getById(classroomId);
            if (classroom == null) {
                log.warn("教室不存在: {}", classroomId);
                return;
            }

            // 获取教学楼信息
            Building building = buildingService.getBuildingById(classroom.getBuildingId());
            if (building == null) {
                log.warn("教学楼不存在: {}", classroom.getBuildingId());
                return;
            }

            String campusId = building.getCampusId();

            // 查询所有有效的假期（status = 1）
            List<Holiday> holidays = holidayService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Holiday>()
                            .eq("status", 1)
                            .and(wrapper -> wrapper
                                    .isNull("campus_id")  // 全校假期（campus_id为null）
                                    .or()
                                    .eq("campus_id", campusId)  // 该校区特定假期
                            )
            );

            // 检查日期是否在任何一个假期范围内
            for (Holiday holiday : holidays) {
                if (holiday.getStartDate() != null && holiday.getEndDate() != null) {
                    // 检查日期是否在假期范围内（包含起止日期）
                    if (!date.isBefore(holiday.getStartDate()) && !date.isAfter(holiday.getEndDate())) {
                        throw new IllegalArgumentException(
                                String.format("该日期在假期期间（%s：%s 至 %s），不能占用教室",
                                        holiday.getName(),
                                        holiday.getStartDate(),
                                        holiday.getEndDate())
                        );
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // 重新抛出假期冲突异常
            throw e;
        } catch (Exception e) {
            // 如果查询假期失败，记录日志但不阻止预约（避免因假期服务问题影响预约功能）
            log.error("检查假期冲突时发生错误: {}", e.getMessage(), e);
        }
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
        reservation.setApproveTime(LocalDateTime.now(BEIJING_ZONE)); // 使用北京时间
        reservation.setUpdatedAt(LocalDateTime.now(BEIJING_ZONE)); // 使用北京时间

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