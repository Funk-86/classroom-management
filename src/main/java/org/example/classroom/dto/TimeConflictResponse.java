package org.example.classroom.dto;

public class TimeConflictResponse {
    private boolean hasConflict;
    private String message;
    private String conflictingReservationId;

    // 构造函数
    public TimeConflictResponse() {}

    public TimeConflictResponse(boolean hasConflict, String message) {
        this.hasConflict = hasConflict;
        this.message = message;
    }

    public TimeConflictResponse(boolean hasConflict, String message, String conflictingReservationId) {
        this.hasConflict = hasConflict;
        this.message = message;
        this.conflictingReservationId = conflictingReservationId;
    }

    // Getter和Setter
    public boolean isHasConflict() { return hasConflict; }
    public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getConflictingReservationId() { return conflictingReservationId; }
    public void setConflictingReservationId(String conflictingReservationId) { this.conflictingReservationId = conflictingReservationId; }
}