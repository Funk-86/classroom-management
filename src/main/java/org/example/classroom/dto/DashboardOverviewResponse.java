package org.example.classroom.dto;

import org.example.classroom.entity.Announcement;
import org.example.classroom.entity.Classroom;
import org.example.classroom.entity.Campus;
import org.example.classroom.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardOverviewResponse {
    private Map<String, Object> stats;
    private List<Reservation> recentReservations;
    private List<Announcement> latestAnnouncements;
    private List<Classroom> popularClassrooms;
    private List<Campus> campuses;
    private Map<String, Object> systemInfo;
    private LocalDateTime generatedAt;

    // 构造函数
    public DashboardOverviewResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    // Getter和Setter
    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public List<Reservation> getRecentReservations() { return recentReservations; }
    public void setRecentReservations(List<Reservation> recentReservations) { this.recentReservations = recentReservations; }

    public List<Announcement> getLatestAnnouncements() { return latestAnnouncements; }
    public void setLatestAnnouncements(List<Announcement> latestAnnouncements) { this.latestAnnouncements = latestAnnouncements; }

    public List<Classroom> getPopularClassrooms() { return popularClassrooms; }
    public void setPopularClassrooms(List<Classroom> popularClassrooms) { this.popularClassrooms = popularClassrooms; }

    public List<Campus> getCampuses() { return campuses; }
    public void setCampuses(List<Campus> campuses) { this.campuses = campuses; }

    public Map<String, Object> getSystemInfo() { return systemInfo; }
    public void setSystemInfo(Map<String, Object> systemInfo) { this.systemInfo = systemInfo; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}