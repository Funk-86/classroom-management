package org.example.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.dto.LoginResponse;
import org.example.classroom.entity.User;
import java.util.List;

public interface UserService extends IService<User> {
    LoginResponse login(String username, String password, Integer role);
    User getUserByUsername(String username);
    User getUserById(String userId);
    boolean updateUser(User user);
    LoginResponse validateToken(String token);
    boolean logout(String sessionId);
    long count();
    List<User> getAllUsers();
    List<User> getUsersByPage(int pageNum, int pageSize);
    List<User> getUsersByRole(Integer role);
    long getUserCount();

    // 新增的方法（移除状态相关方法）
    boolean addUser(User user);
    boolean deleteUser(String userId);
    List<User> searchUsers(String keyword);
    List<User> searchUsersByPage(String keyword, int pageNum, int pageSize);
    long getSearchCount(String keyword);
    boolean changePassword(String userId, String newPassword);
    User getUserDetail(String userId);

    // 新增：学院相关查询
    List<User> getUsersByCollege(String collegeId);
    List<User> getUsersByCollegeAndRole(String collegeId, Integer role);
    List<User> getUsersByRoleWithCollege(Integer role, String collegeId);

    List<User> getUsersByClass(String classId);
    List<User> getUsersByClassAndRole(String classId, Integer role);
    
    // Session管理方法
    int getActiveSessionCount();
    boolean forceLogoutUser(String userId);
}