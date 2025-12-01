package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.dto.LoginResponse;
import org.example.classroom.entity.User;
import org.example.classroom.entity.UserSession;
import org.example.classroom.mapper.UserMapper;
import org.example.classroom.mapper.UserSessionMapper;
import org.example.classroom.service.UserService;
import org.example.classroom.util.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long SESSION_EXPIRE_MS = 30 * 60 * 1000;

    // Session存储
    private final Map<String, UserSessionInfo> sessionStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public UserServiceImpl() {
        // 延迟启动定时任务，避免在数据库未就绪时执行
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredSessions();
            } catch (Exception e) {
                // 静默处理，避免定时任务异常影响应用
                log.debug("定时清理任务执行失败（可能是数据库未就绪）", e);
            }
        }, 5, 5, TimeUnit.MINUTES); // 延迟5分钟启动，每5分钟执行一次
    }

    @Override
    public LoginResponse login(String username, String password, Integer role) {
        try {
            User user = baseMapper.selectByUsernameAndRole(username, role);
            if (user == null) {
                return new LoginResponse(false, "用户不存在或角色不匹配");
            }
            // 使用BCrypt验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return new LoginResponse(false, "密码错误");
            }

            String sessionId = generateSessionId(user);
            Date loginTime = new Date();
            Date expireTime = new Date(loginTime.getTime() + SESSION_EXPIRE_MS);

            // 存储Session到数据库
            UserSession userSession = new UserSession();
            userSession.setSessionId(sessionId);
            userSession.setUserId(user.getUserId());
            userSession.setLoginTime(loginTime);
            userSession.setExpireTime(expireTime);
            userSession.setLastAccessTime(loginTime);

            try {
                userSessionMapper.insert(userSession);
            } catch (Exception e) {
                log.error("保存Session到数据库失败", e);
                // 如果数据库操作失败，仍然返回登录成功，但记录错误
                // 可以考虑使用内存存储作为备选方案
                throw new RuntimeException("数据库连接失败，无法保存登录会话: " + e.getMessage(), e);
            }

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getUserId(),
                    user.getUserName(),
                    user.getUserRole(),
                    user.getCollegeId(),  // 添加学院ID
                    user.getClassId()     // 添加班级ID
            );
            return new LoginResponse(true, "登录成功", sessionId, userInfo);
        } catch (RuntimeException e) {
            // 重新抛出运行时异常
            throw e;
        } catch (Exception e) {
            log.error("登录过程中发生未知错误", e);
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public LoginResponse validateToken(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return new LoginResponse(false, "Session ID不能为空");
        }

        // 从数据库查询Session
        UserSession userSession = userSessionMapper.selectBySessionId(sessionId);
        if (userSession == null) {
            return new LoginResponse(false, "Session无效或已过期");
        }

        Date currentTime = new Date();
        // 检查是否过期
        if (userSession.getExpireTime().before(currentTime)) {
            userSessionMapper.deleteById(sessionId);
            return new LoginResponse(false, "Session已过期，请重新登录");
        }

        User user = getById(userSession.getUserId());
        if (user != null) {
            // 更新最后访问时间和过期时间（滑动过期）
            Date newExpireTime = new Date(currentTime.getTime() + SESSION_EXPIRE_MS);
            userSessionMapper.updateAccessTime(sessionId, currentTime, newExpireTime);

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getUserId(),
                    user.getUserName(),
                    user.getUserRole(),
                    user.getCollegeId(),  // 添加学院ID
                    user.getClassId()     // 添加班级ID
            );
            return new LoginResponse(true, "Session有效", sessionId, userInfo);
        } else {
            // 用户不存在，删除session
            userSessionMapper.deleteById(sessionId);
            return new LoginResponse(false, "用户不存在");
        }
    }

    @Override
    public boolean logout(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        // 从数据库删除session
        int deleted = userSessionMapper.deleteById(sessionId);
        // 同时从内存中删除（如果存在）
        sessionStore.remove(sessionId);
        return deleted > 0;
    }

    @Override
    public User getUserByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public User getUserById(String userId) {
        return getById(userId);
    }

    @Override
    public boolean updateUser(User user) {
        return updateById(user);
    }

    @Override
    public List<User> getAllUsers() {
        return baseMapper.selectAllUsers();
    }

    @Override
    public List<User> getUsersByPage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        int offset = (pageNum - 1) * pageSize;
        return baseMapper.selectUsersByPage(offset, pageSize);
    }

    @Override
    public List<User> getUsersByRole(Integer role) {
        if (role == null) {
            return getAllUsers();
        }
        return baseMapper.selectUsersByRole(role);
    }

    @Override
    public long getUserCount() {
        return count();
    }

    @Override
    public long count() {
        return baseMapper.selectCount(null);
    }

    @Override
    @Transactional
    public boolean addUser(User user) {
        if (user == null || !StringUtils.hasText(user.getUserName())) {
            return false;
        }

        // 检查用户名是否已存在
        User existingUser = baseMapper.selectByUsername(user.getUserName());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        if (user.getUserRole() == null) {
            user.setUserRole(0); // 默认学生角色
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // 生成随机密码
            String randomPassword = passwordEncoder.generateRandomPassword(12);
            user.setPassword(passwordEncoder.encode(randomPassword));
            log.warn("用户 {} 使用随机生成的密码，请及时修改", user.getUserName());
        } else {
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return save(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }

        // 先强制退出该用户的所有会话
        forceLogoutUser(userId);

        // 然后删除用户
        return removeById(userId);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllUsers();
        }
        return baseMapper.searchUsers(keyword.trim());
    }

    @Override
    public List<User> searchUsersByPage(String keyword, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        int offset = (pageNum - 1) * pageSize;

        if (!StringUtils.hasText(keyword)) {
            return getUsersByPage(pageNum, pageSize);
        }

        return baseMapper.searchUsersByPage(keyword.trim(), offset, pageSize);
    }

    @Override
    public long getSearchCount(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getUserCount();
        }
        return baseMapper.countByKeyword(keyword.trim());
    }

    @Override
    @Transactional
    public boolean changePassword(String userId, String newPassword) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(newPassword)) {
            return false;
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        int result = baseMapper.updatePassword(userId, encodedPassword);
        if (result > 0) {
            // 密码修改成功后强制退出该用户的所有会话
            forceLogoutUser(userId);
            return true;
        }
        return false;
    }

    @Override
    public User getUserDetail(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        return getById(userId);
    }

    // 新增：学院相关查询方法
    @Override
    public List<User> getUsersByCollege(String collegeId) {
        if (!StringUtils.hasText(collegeId)) {
            return getAllUsers();
        }
        return baseMapper.selectUsersByCollege(collegeId);
    }

    @Override
    public List<User> getUsersByCollegeAndRole(String collegeId, Integer role) {
        if (!StringUtils.hasText(collegeId)) {
            return getUsersByRole(role);
        }
        return baseMapper.selectUsersByCollegeAndRole(collegeId, role);
    }

    @Override
    public List<User> getUsersByRoleWithCollege(Integer role, String collegeId) {
        if (StringUtils.hasText(collegeId)) {
            return getUsersByCollegeAndRole(collegeId, role);
        } else {
            return getUsersByRole(role);
        }
    }

    @Override
    public List<User> getUsersByClass(String classId) {
        if (!StringUtils.hasText(classId)) {
            return getAllUsers();
        }
        return baseMapper.selectUsersByClass(classId);
    }


    @Override
    public List<User> getUsersByClassAndRole(String classId, Integer role) {
        if (!StringUtils.hasText(classId)) {
            return getUsersByRole(role);
        }
        if (role == null) {
            return getUsersByClass(classId);
        }
        return baseMapper.selectUsersByClassAndRole(classId, role);
    }

    // 定时清理过期session（每5分钟执行一次）
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredSessions() {
        try {
            int deletedCount = userSessionMapper.deleteExpiredSessions(new Date());
            if (deletedCount > 0) {
                log.info("清理了 {} 个过期Session", deletedCount);
            }
        } catch (org.springframework.jdbc.CannotGetJdbcConnectionException e) {
            // 数据库连接失败，记录警告但不影响其他功能
            log.warn("清理过期Session失败：数据库连接不可用，将稍后重试", e);
        } catch (Exception e) {
            // 其他异常记录错误日志
            log.error("清理过期Session时发生错误", e);
        }
    }

    private String generateSessionId(User user) {
        String raw = user.getUserId() + user.getUserName() + System.currentTimeMillis() + Math.random();
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getActiveSessionCount() {
        return userSessionMapper.countActiveSessions(new Date());
    }

    @Override
    public boolean forceLogoutUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        int deleted = userSessionMapper.deleteByUserId(userId);
        // 同时清理内存中的session
        sessionStore.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
        return deleted > 0;
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        log.info("正在关闭UserService的调度器...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("UserService调度器已关闭");
    }

    private String extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String sessionId = token.substring(7);

            // 直接从 sessionStore 获取用户信息
            UserSessionInfo sessionInfo = sessionStore.get(sessionId);
            if (sessionInfo != null) {
                return sessionInfo.getUserId(); // 返回正确的用户ID
            }

            throw new IllegalArgumentException("Session无效或已过期");
        }
        throw new IllegalArgumentException("无效的token");
    }

    private static class UserSessionInfo {
        private final String sessionId;
        private final String userId;
        private long loginTime;

        public UserSessionInfo(String sessionId, String userId, long loginTime) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.loginTime = loginTime;
        }

        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public long getLoginTime() { return loginTime; }
        public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
    }
}