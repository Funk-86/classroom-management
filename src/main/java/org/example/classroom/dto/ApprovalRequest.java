package org.example.classroom.dto;

import java.time.LocalDateTime;

public class ApprovalRequest {
    private String reservationId;
    private Integer action; // 1: 通过, 2: 拒绝
    private String notes;   // 审批意见/拒绝原因

    // 构造函数
    public ApprovalRequest() {}

    public ApprovalRequest(String reservationId, Integer action, String notes) {
        this.reservationId = reservationId;
        this.action = action;
        this.notes = notes;
    }

    // Getter和Setter
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // 验证方法
    public String validate() {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "预约ID不能为空";
        }
        if (action == null || (action != 1 && action != 2)) {
            return "审批动作无效（1:通过, 2:拒绝）";
        }
        if (action == 2 && (notes == null || notes.trim().isEmpty())) {
            return "拒绝时必须填写拒绝原因";
        }
        return null;
    }
}