package org.example.classroom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.classroom.dto.ApprovalRequest;
import org.example.classroom.dto.R;
import org.example.classroom.entity.Reservation;
import org.example.classroom.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    @Autowired
    private ReservationService reservationService;

    // 审批预约
    @PostMapping("/approve")
    public R approveReservation(@RequestBody ApprovalRequest approvalRequest,
                                @RequestHeader("X-User-Id") String approverId) {
        // 验证请求参数
        String validationError = approvalRequest.validate();
        if (validationError != null) {
            return R.error(400, validationError);
        }

        try {
            boolean result = reservationService.approveReservation(approvalRequest, approverId);
            if (result) {
                String actionText = approvalRequest.getAction() == 1 ? "通过" : "拒绝";
                return R.ok("预约已" + actionText);
            } else {
                return R.error("审批失败");
            }
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error("审批操作失败: " + e.getMessage());
        }
    }

    // 获取待审批列表
    @GetMapping("/pending")
    public R getPendingApprovals(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            IPage<Reservation> pendingPage = reservationService.getPendingApprovals(page, size, startDate, endDate);
            return R.ok().put("data", pendingPage);
        } catch (Exception e) {
            return R.error("获取待审批列表失败: " + e.getMessage());
        }
    }

    // 获取今日待审批
    @GetMapping("/pending/today")
    public R getTodayPendingApprovals() {
        try {
            List<Reservation> todayPending = reservationService.getTodayPendingApprovals();
            return R.ok().put("data", todayPending);
        } catch (Exception e) {
            return R.error("获取今日待审批失败: " + e.getMessage());
        }
    }

    // 获取审批历史
    @GetMapping("/history")
    public R getApprovalHistory(@RequestHeader("X-User-Id") String approverId,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            IPage<Reservation> historyPage = reservationService.getApprovalHistory(approverId, page, size, startDate, endDate);
            return R.ok().put("data", historyPage);
        } catch (Exception e) {
            return R.error("获取审批历史失败: " + e.getMessage());
        }
    }

    // 审批统计
    @GetMapping("/stats")
    public R getApprovalStats(@RequestHeader("X-User-Id") String approverId) {
        try {
            long pendingCount = reservationService.countByStatus(0);
            long approvedCount = reservationService.countByStatus(1);
            long rejectedCount = reservationService.countByStatus(2);

            // 今日审批统计
            List<Reservation> todayApprovals = reservationService.getApprovalHistory(
                    approverId, 1, 100, LocalDate.now(), LocalDate.now()).getRecords();
            long todayApproved = todayApprovals.stream().filter(r -> r.getStatus() == 1).count();
            long todayRejected = todayApprovals.stream().filter(r -> r.getStatus() == 2).count();

            return R.ok().put("data", new ApprovalStats(
                    pendingCount, approvedCount, rejectedCount, todayApproved, todayRejected
            ));
        } catch (Exception e) {
            return R.error("获取审批统计失败: " + e.getMessage());
        }
    }

    // 审批统计内部类
    private static class ApprovalStats {
        public long pendingCount;
        public long approvedCount;
        public long rejectedCount;
        public long todayApproved;
        public long todayRejected;

        public ApprovalStats(long pendingCount, long approvedCount, long rejectedCount,
                             long todayApproved, long todayRejected) {
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
            this.todayApproved = todayApproved;
            this.todayRejected = todayRejected;
        }
    }
}