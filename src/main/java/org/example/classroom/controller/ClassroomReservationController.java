package org.example.classroom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classroom.dto.*;
import org.example.classroom.entity.Reservation;
import org.example.classroom.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ClassroomReservationController {

    @Autowired
    private ReservationService reservationService;

    // 获取所有预约（分页）
    @GetMapping("/all")
    public R getAllReservations(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            IPage<Reservation> reservationPage = reservationService.getAllReservations(page, size, status, startDate, endDate);

            // 转换为响应DTO
            List<ReservationResponse> responseList = reservationPage.getRecords()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 创建分页响应
            Page<ReservationResponse> responsePage = new Page<>(
                    reservationPage.getCurrent(),
                    reservationPage.getSize(),
                    reservationPage.getTotal()
            );
            responsePage.setRecords(responseList);

            return R.ok().put("data", responsePage);
        } catch (Exception e) {
            return R.error("获取预约列表失败: " + e.getMessage());
        }
    }

    // 获取最近预约
    @GetMapping("/recent")
    public R getRecentReservations(@RequestParam(defaultValue = "0") Integer status,
                                   @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Reservation> reservations = reservationService.getRecentReservationsByStatus(status, limit);
            List<ReservationResponse> responses = reservations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return R.ok().put("data", responses);
        } catch (Exception e) {
            return R.error("获取最近预约失败: " + e.getMessage());
        }
    }


    // 创建预约
    @PostMapping
    public R createReservation(@jakarta.validation.Valid @RequestBody ReservationRequest request) {
        // 验证请求参数
        String validationError = request.validate();
        if (validationError != null) {
            return R.error(400, validationError);
        }

        try {
            // 转换DTO到Entity
            Reservation reservation = convertToEntity(request);
            Reservation result = reservationService.createReservation(reservation);

            // 转换Entity到Response DTO
            ReservationResponse response = convertToResponse(result);
            return R.ok("预约成功").put("data", response);
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error("预约失败: " + e.getMessage());
        }
    }

    // 检查时间冲突
    @PostMapping("/checkConflict")
    public R checkTimeConflict(@RequestBody TimeConflictRequest request) {
        // 验证请求参数
        String validationError = request.validate();
        if (validationError != null) {
            return R.error(400, validationError);
        }

        try {
            boolean hasConflict = reservationService.checkTimeConflict(
                    request.getClassroomId(),
                    request.getDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            TimeConflictResponse response = new TimeConflictResponse(
                    hasConflict,
                    hasConflict ? "时间冲突" : "时间可用"
            );
            return R.ok().put("data", response);
        } catch (Exception e) {
            return R.error("检查时间冲突失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R getReservationById(@PathVariable String id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation != null) {
                ReservationResponse response = convertToResponse(reservation);
                return R.ok().put("data", response);
            } else {
                return R.error(404, "预约不存在");
            }
        } catch (Exception e) {
            return R.error("获取预约详情失败: " + e.getMessage());
        }
    }

    // 获取用户的所有预约
    @GetMapping("/user/{userId}")
    public R getUserReservations(@PathVariable String userId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<Reservation> reservations = reservationService.getUserReservations(userId, page, size);
            List<ReservationResponse> responses = reservations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return R.ok().put("data", responses);
        } catch (Exception e) {
            return R.error("获取用户预约失败: " + e.getMessage());
        }
    }

    // 获取教室的预约记录
    @GetMapping("/classroom/{classroomId}")
    public R getClassroomReservations(@PathVariable String classroomId,
                                      @RequestParam LocalDate date,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<Reservation> reservations = reservationService.getClassroomReservations(classroomId, date, page, size);
            List<ReservationResponse> responses = reservations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return R.ok().put("data", responses);
        } catch (Exception e) {
            return R.error("获取教室预约记录失败: " + e.getMessage());
        }
    }

    // 取消预约
    @DeleteMapping("/{id}")
    public R cancelReservation(@PathVariable String id) {
        if (reservationService.cancelReservation(id)) {
            return R.ok("预约已取消");
        } else {
            return R.error("取消预约失败");
        }
    }

    @GetMapping("/user/{userId}/history")
    public R getUserHistoryReservations(@PathVariable String userId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        try {
            IPage<UserReservationHistoryResponse> historyPage =
                    reservationService.getUserHistoryReservations(userId, page, size);

            return R.ok().put("data", historyPage);
        } catch (Exception e) {
            return R.error("获取历史预约记录失败: " + e.getMessage());
        }
    }

    // 按时间范围获取用户历史预约记录
    @GetMapping("/user/{userId}/history/date-range")
    public R getUserHistoryReservationsByDateRange(@PathVariable String userId,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        try {
            IPage<UserReservationHistoryResponse> historyPage =
                    reservationService.getUserHistoryReservationsByDateRange(userId, startDate, endDate, page, size);

            return R.ok().put("data", historyPage);
        } catch (Exception e) {
            return R.error("获取历史预约记录失败: " + e.getMessage());
        }
    }

    // 添加审批相关的方法
    @PostMapping("/approve")
    public R approveReservation(@RequestBody ApprovalRequest request,
                                @RequestParam String approverId) {
        try {
            boolean result = reservationService.approveReservation(request, approverId);
            if (result) {
                return R.ok("审批操作成功");
            } else {
                return R.error("审批操作失败");
            }
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error("审批操作失败: " + e.getMessage());
        }
    }

    // 获取审批历史
    @GetMapping("/approval/history")
    public R getApprovalHistory(@RequestParam String approverId,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            IPage<Reservation> historyPage = reservationService.getApprovalHistory(
                    approverId, page, size, startDate, endDate);
            return R.ok().put("data", historyPage);
        } catch (Exception e) {
            return R.error("获取审批历史失败: " + e.getMessage());
        }
    }

    // 转换方法
    private Reservation convertToEntity(ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setClassroomId(request.getClassroomId());
        reservation.setUserId(request.getUserId());
        reservation.setPurpose(request.getPurpose());
        reservation.setDate(request.getDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        return reservation;
    }

    private ReservationResponse convertToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setReservationId(reservation.getReservationId());
        response.setClassroomId(reservation.getClassroomId());
        response.setUserId(reservation.getUserId());
        response.setPurpose(reservation.getPurpose());
        response.setDate(reservation.getDate());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setStatus(reservation.getStatus());
        response.setAdminNotes(reservation.getAdminNotes());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setUpdatedAt(reservation.getUpdatedAt());
        response.setApproverId(reservation.getApproverId());
        response.setApproveTime(reservation.getApproveTime());
        response.setRejectReason(reservation.getRejectReason());

        // 设置教室和教学楼名称（如果实体中有这些字段）
        if (reservation.getClassroomName() != null) {
            response.setClassroomName(reservation.getClassroomName());
        }
        if (reservation.getBuildingName() != null) {
            response.setBuildingName(reservation.getBuildingName());
        }
        if (reservation.getStudentName() != null) {
            response.setUserName(reservation.getStudentName());
        }
        if (reservation.getApproverName() != null) {
            response.setApproverName(reservation.getApproverName());
        }

        return response;
    }
}