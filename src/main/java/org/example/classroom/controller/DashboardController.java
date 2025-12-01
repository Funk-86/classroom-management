package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.*;
import org.example.classroom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private CampusService campusService;

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/stats")
    public R getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总教室数
            long totalClassrooms = classroomService.count();
            stats.put("classroomCount", totalClassrooms);

            // 总用户数
            long totalUsers = userService.count();
            stats.put("userCount", totalUsers);

            // 待审核预约数（状态为0）
            long pendingReservations = reservationService.countByStatus(0);
            stats.put("pendingReservationCount", pendingReservations);

            // 活跃公告数（当前时间在有效期内）
            long activeAnnouncements = announcementService.countActiveAnnouncements();
            stats.put("activeAnnouncementCount", activeAnnouncements);

            return R.ok().put("data", stats);
        } catch (Exception e) {
            return R.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近预约记录
     */
    @GetMapping("/recent-reservations")
    public R getRecentReservations(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Reservation> recentReservations = reservationService.getRecentReservations(limit);
            return R.ok().put("data", recentReservations);
        } catch (Exception e) {
            return R.error("获取最近预约失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system-info")
    public R getSystemInfo() {
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("serverTime", LocalDateTime.now());
            systemInfo.put("systemVersion", "1.0.0");
            systemInfo.put("lastUpdate", "2025-01-02");

            return R.ok().put("data", systemInfo);
        } catch (Exception e) {
            return R.error("获取系统信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest-announcements")
    public R getLatestAnnouncements(@RequestParam(defaultValue = "3") int limit) {
        try {
            List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
            return R.ok().put("data", announcements);
        } catch (Exception e) {
            return R.error("获取最新公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门教室
     */
    @GetMapping("/popular-classrooms")
    public R getPopularClassrooms(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Classroom> classrooms = classroomService.getPopularClassrooms(limit);
            return R.ok().put("data", classrooms);
        } catch (Exception e) {
            return R.error("获取热门教室失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有校区
     */
    @GetMapping("/campuses")
    public R getAllCampuses() {
        try {
            List<Campus> campuses = campusService.getAllCampuses();
            return R.ok().put("data", campuses);
        } catch (Exception e) {
            return R.error("获取校区信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取校区建筑
     */
    @GetMapping("/buildings/{campusId}")
    public R getBuildingsByCampus(@PathVariable String campusId) {
        try {
            List<Building> buildings = buildingService.getBuildingsByCampus(campusId);
            return R.ok().put("data", buildings);
        } catch (Exception e) {
            return R.error("获取建筑信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取仪表板完整数据
     */
    @GetMapping("/overview")
    public R getDashboardOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();

            // 统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("classroomCount", classroomService.count());
            stats.put("userCount", userService.count());
            stats.put("pendingReservationCount", reservationService.countByStatus(0));
            stats.put("activeAnnouncementCount", announcementService.countActiveAnnouncements());
            overview.put("stats", stats);

            // 最近预约
            overview.put("recentReservations", reservationService.getRecentReservations(5));

            // 最新公告
            overview.put("latestAnnouncements", announcementService.getLatestAnnouncements(3));

            // 热门教室
            overview.put("popularClassrooms", classroomService.getPopularClassrooms(5));

            // 校区列表
            overview.put("campuses", campusService.getAllCampuses());

            // 系统信息
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("serverTime", LocalDateTime.now());
            systemInfo.put("lastUpdated", LocalDateTime.now());
            overview.put("systemInfo", systemInfo);

            return R.ok().put("data", overview);
        } catch (Exception e) {
            return R.error("获取仪表板数据失败: " + e.getMessage());
        }
    }
}