package org.example.classroom.dto;

import java.time.LocalDateTime;

public class DashboardStatsResponse {
    private long classroomCount;
    private long userCount;
    private long pendingReservationCount;
    private long activeAnnouncementCount;
    private LocalDateTime lastUpdated;

    // 构造函数
    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long classroomCount, long userCount, long pendingReservationCount, long activeAnnouncementCount) {
        this.classroomCount = classroomCount;
        this.userCount = userCount;
        this.pendingReservationCount = pendingReservationCount;
        this.activeAnnouncementCount = activeAnnouncementCount;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getter和Setter
    public long getClassroomCount() { return classroomCount; }
    public void setClassroomCount(long classroomCount) { this.classroomCount = classroomCount; }

    public long getUserCount() { return userCount; }
    public void setUserCount(long userCount) { this.userCount = userCount; }

    public long getPendingReservationCount() { return pendingReservationCount; }
    public void setPendingReservationCount(long pendingReservationCount) { this.pendingReservationCount = pendingReservationCount; }

    public long getActiveAnnouncementCount() { return activeAnnouncementCount; }
    public void setActiveAnnouncementCount(long activeAnnouncementCount) { this.activeAnnouncementCount = activeAnnouncementCount; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}