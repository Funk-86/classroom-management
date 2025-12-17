package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.AttendanceRecord;
import org.example.classroom.entity.AttendanceSession;
import org.example.classroom.mapper.AttendanceRecordMapper;
import org.example.classroom.mapper.AttendanceSessionMapper;
import org.example.classroom.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceSessionMapper, AttendanceSession> implements AttendanceService {

    @Autowired
    private AttendanceSessionMapper sessionMapper;

    @Autowired
    private AttendanceRecordMapper recordMapper;

    @Autowired
    private org.example.classroom.service.ClassroomOccupationService classroomOccupationService;

    @Override
    @Transactional
    public AttendanceSession createSession(AttendanceSession session) {
        try {
            // 设置默认值
            if (session.getRadius() == null) {
                session.setRadius(100); // 默认100米
            }
            if (session.getStatus() == null) {
                session.setStatus(1); // 默认进行中
            }

            // 验证必填字段
            if (session.getCourseId() == null || session.getCourseId().trim().isEmpty()) {
                throw new RuntimeException("课程ID不能为空");
            }
            if (session.getLatitude() == null || session.getLongitude() == null) {
                throw new RuntimeException("签到位置不能为空");
            }
            if (session.getStartTime() == null || session.getEndTime() == null) {
                throw new RuntimeException("签到时间不能为空");
            }

            // 如果指定了教室，检查教室占用冲突
            if (session.getClassroomId() != null && !session.getClassroomId().trim().isEmpty()) {
                java.time.LocalDate checkDate = session.getStartTime().toLocalDate();
                java.time.LocalTime checkStartTime = session.getStartTime().toLocalTime();
                java.time.LocalTime checkEndTime = session.getEndTime().toLocalTime();

                org.example.classroom.dto.ClassroomConflictResult conflictResult =
                        classroomOccupationService.checkClassroomOccupation(
                                session.getClassroomId(),
                                checkDate,
                                checkStartTime,
                                checkEndTime,
                                null,
                                null
                        );

                if (conflictResult.isHasConflict()) {
                    throw new RuntimeException("教室在该时间段已被占用: " + conflictResult.getMessage());
                }
            }

            save(session);

            // 确保sessionId已生成
            if (session.getSessionId() == null) {
                throw new RuntimeException("创建签到活动失败，未生成活动ID");
            }

            return sessionMapper.selectSessionWithDetail(session.getSessionId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建签到活动失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AttendanceSession getSessionDetail(String sessionId) {
        return sessionMapper.selectSessionWithDetail(sessionId);
    }

    @Override
    public List<AttendanceSession> getTeacherSessions(String teacherId, Integer limit) {
        if (limit == null) limit = 20;
        return sessionMapper.selectTeacherSessions(teacherId, limit);
    }

    @Override
    public AttendanceSession getActiveSessionByCourse(String courseId) {
        return sessionMapper.selectActiveSessionByCourse(courseId);
    }

    @Override
    public List<AttendanceSession> getCourseSessions(String courseId) {
        return sessionMapper.selectCourseSessions(courseId);
    }

    @Override
    public List<AttendanceSession> getActiveSessionsForStudent(String studentId) {
        // 先查询所有进行中的签到活动（不限制时间，在Java代码中判断）
        List<AttendanceSession> allActiveSessions = sessionMapper.selectActiveSessionsForStudentWithoutTimeCheck(studentId);

        if (allActiveSessions == null || allActiveSessions.isEmpty()) {
            System.out.println("未找到学生 " + studentId + " 的签到活动（状态为1）");
            return java.util.Collections.emptyList();
        }

        System.out.println("找到 " + allActiveSessions.size() + " 个进行中的签到活动（状态为1）");

        // 获取当前时间（东八区）
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        LocalDateTime now = ZonedDateTime.now(shanghaiZone).toLocalDateTime();

        System.out.println("当前时间（东八区）: " + now);

        // 获取学生已签到的活动ID列表
        List<String> checkedInSessionIds = recordMapper.selectList(
                        new QueryWrapper<AttendanceRecord>()
                                .eq("student_id", studentId)
                                .select("session_id")
                ).stream()
                .map(AttendanceRecord::getSessionId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        System.out.println("学生已签到的活动ID: " + checkedInSessionIds);

        // 过滤出当前时间在签到时间范围内且未签到的活动
        List<AttendanceSession> activeSessions = allActiveSessions.stream()
                .filter(session -> {
                    // 检查是否已签到
                    if (checkedInSessionIds.contains(session.getSessionId())) {
                        System.out.println("签到活动 " + session.getSessionId() + " 已签到，跳过");
                        return false;
                    }

                    if (session.getStartTime() == null || session.getEndTime() == null) {
                        System.out.println("签到活动 " + session.getSessionId() + " 的时间为空");
                        return false;
                    }

                    LocalDateTime startTime = session.getStartTime();
                    LocalDateTime endTime = session.getEndTime();

                    // 判断：当前时间 >= 开始时间 且 当前时间 <= 结束时间
                    boolean isActive = !now.isBefore(startTime) && !now.isAfter(endTime);

                    // 调试日志
                    System.out.println(String.format("签到活动 %s: 当前=%s, 开始=%s, 结束=%s, 是否活跃=%s",
                            session.getSessionId(), now, startTime, endTime, isActive));

                    return isActive;
                })
                .collect(java.util.stream.Collectors.toList());

        System.out.println("过滤后找到 " + activeSessions.size() + " 个未签到的当前时间范围内的签到活动");

        return activeSessions;
    }

    @Override
    @Transactional
    public AttendanceRecord checkIn(String sessionId, String studentId, BigDecimal latitude, BigDecimal longitude) {
        // 获取签到活动
        AttendanceSession session = sessionMapper.selectSessionWithDetail(sessionId);
        if (session == null) {
            throw new RuntimeException("签到活动不存在");
        }

        // 检查签到活动状态
        if (session.getStatus() != 1) {
            throw new RuntimeException("签到活动已结束或已取消");
        }

        // 获取当前时间（东八区）
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        LocalDateTime now = ZonedDateTime.now(shanghaiZone).toLocalDateTime();

        // 调试日志：输出时间信息
        System.out.println("=== 签到时间检查 ===");
        System.out.println("当前时间（东八区）: " + now);
        System.out.println("签到开始时间: " + session.getStartTime());
        System.out.println("签到结束时间: " + session.getEndTime());
        System.out.println("当前时间是否在开始时间之前: " + now.isBefore(session.getStartTime()));
        System.out.println("当前时间是否在结束时间之后: " + now.isAfter(session.getEndTime()));

        // 判断是否在签到时间内：当前时间 >= 开始时间 且 当前时间 <= 结束时间
        // 允许在结束时间点签到
        boolean isBeforeStart = now.isBefore(session.getStartTime());
        boolean isAfterEnd = now.isAfter(session.getEndTime());

        if (isBeforeStart || isAfterEnd) {
            String errorMsg = String.format("不在签到时间内 - 当前时间: %s, 签到时间: %s 至 %s",
                    now, session.getStartTime(), session.getEndTime());
            throw new RuntimeException(errorMsg);
        }

        // 检查是否已签到
        AttendanceRecord existing = recordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AttendanceRecord>()
                        .eq("session_id", sessionId)
                        .eq("student_id", studentId)
        );
        if (existing != null) {
            throw new RuntimeException("您已经签到过了");
        }

        // 计算距离
        double distance = calculateDistance(
                session.getLatitude(), session.getLongitude(),
                latitude, longitude
        );

        // 创建签到记录
        AttendanceRecord record = new AttendanceRecord();
        record.setSessionId(sessionId);
        record.setStudentId(studentId);
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        record.setDistance((int) distance);
        record.setCheckinTime(now);

        // 判断签到状态
        if (distance > session.getRadius()) {
            record.setCheckinStatus(2); // 距离过远
            record.setRemark("距离签到点" + String.format("%.2f", distance) + "米，超出允许范围" + session.getRadius() + "米");
        } else {
            record.setCheckinStatus(1); // 成功
        }

        recordMapper.insert(record);

        // 设置status字段用于返回（业务逻辑字段，不在数据库中）
        // checkinStatus: 1=成功, 2=距离过远, 3=超时
        // status: 0=正常, 1=迟到, 2=缺勤
        if (record.getCheckinStatus() == 1) {
            record.setStatus(0); // 正常签到
        } else {
            record.setStatus(2); // 缺勤/失败
        }

        return record;
    }

    @Override
    public List<AttendanceRecord> getSessionRecords(String sessionId) {
        return recordMapper.selectRecordsBySession(sessionId);
    }

    @Override
    public List<AttendanceRecord> getRelatedSessionsRecords(String sessionId) {
        // 直接查询当前签到活动的记录（因为现在只创建一个签到活动）
        return recordMapper.selectRecordsBySession(sessionId);
    }

    @Override
    public List<AttendanceRecord> getStudentRecords(String studentId, Integer limit) {
        if (limit == null) limit = 20;
        return recordMapper.selectStudentRecords(studentId, limit);
    }

    @Override
    public boolean updateRecordStatus(String recordId, String studentId, String sessionId, Integer status) {
        AttendanceRecord record = null;

        // 如果recordId以temp_开头，说明这是未签到的学生，需要先创建记录
        if (recordId != null && recordId.startsWith("temp_")) {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new RuntimeException("无法创建签到记录：缺少签到活动信息");
            }

            // 检查是否已存在该学生的签到记录
            record = recordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AttendanceRecord>()
                            .eq("session_id", sessionId)
                            .eq("student_id", studentId)
            );

            // 如果不存在，创建新记录
            if (record == null) {
                // 获取签到活动信息
                AttendanceSession session = sessionMapper.selectSessionWithDetail(sessionId);
                if (session == null) {
                    throw new RuntimeException("签到活动不存在");
                }

                // 创建新的签到记录
                record = new AttendanceRecord();
                record.setSessionId(sessionId);
                record.setStudentId(studentId);
                record.setCheckinTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                // 不设置位置信息，因为这是教师手动标记的
                record.setLatitude(null);
                record.setLongitude(null);
                record.setDistance(null);
            }
        } else {
            // 正常的recordId，直接查询
            record = recordMapper.selectById(recordId);
            if (record == null) {
                throw new RuntimeException("签到记录不存在");
            }
        }

        // 验证学生ID是否匹配
        if (!record.getStudentId().equals(studentId)) {
            throw new RuntimeException("学生ID不匹配");
        }

        // status字段不在数据库表中，需要转换为checkinStatus
        if (status == 0 || status == 1) {
            record.setCheckinStatus(1); // 成功签到
            if (record.getCheckinTime() == null) {
                record.setCheckinTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
            }
        } else {
            record.setCheckinStatus(2); // 失败/缺勤
        }

        // 更新status字段用于业务逻辑
        record.setStatus(status);

        // 如果是新记录，执行插入；否则执行更新
        if (record.getRecordId() == null) {
            recordMapper.insert(record);
        } else {
            int result = recordMapper.updateById(record);
            if (result <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        // 使用Haversine公式计算两点间距离
        final int R = 6371000; // 地球半径（米）

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}

