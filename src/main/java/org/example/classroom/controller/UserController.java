package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.dto.UserRequest;
import org.example.classroom.dto.UserResponse;
import org.example.classroom.entity.User;
import org.example.classroom.service.UserService;
import org.example.classroom.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private TokenUtils tokenUtils;

    // 获取当前用户信息
    @GetMapping("/current")
    public R getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String userId = tokenUtils.extractUserIdFromToken(token);
            User user = userService.getUserWithDetail(userId);

            if (user != null) {
                UserResponse response = new UserResponse(user);
                return R.ok().put("data", response);
            } else {
                return R.error("用户不存在");
            }
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据ID获取用户信息
    @GetMapping("/{userId}")
    public R getUserById(@PathVariable String userId) {
        User user = userService.getById(userId);
        if (user != null) {
            UserResponse response = new UserResponse(user);
            return R.ok().put("data", response);
        } else {
            return R.error("用户不存在");
        }
    }

    // 更新用户信息
    @PutMapping("/{userId}")
    public R updateUser(@PathVariable String userId,
                        @RequestBody UserRequest userRequest) {
        // 手动验证请求参数
        String validationError = userRequest.validate();
        if (validationError != null) {
            return R.error(400, validationError);
        }

        try {
            User user = new User();
            user.setUserId(userId);
            user.setUserName(userRequest.getUserName().trim());

            // 处理手机号（可为空）
            if (userRequest.getUserPhone() != null && !userRequest.getUserPhone().trim().isEmpty()) {
                user.setUserPhone(userRequest.getUserPhone().trim());
            } else {
                user.setUserPhone(null);
            }

            // 处理头像（可为空）
            if (userRequest.getUserAvatar() != null && !userRequest.getUserAvatar().trim().isEmpty()) {
                user.setUserAvatar(userRequest.getUserAvatar().trim());
            } else {
                user.setUserAvatar(null);
            }

            // 处理学院ID（可为空）
            if (userRequest.getCollegeId() != null && !userRequest.getCollegeId().trim().isEmpty()) {
                user.setCollegeId(userRequest.getCollegeId().trim());
            } else {
                user.setCollegeId(null);
            }

            // 处理班级ID（可为空）
            if (userRequest.getClassId() != null && !userRequest.getClassId().trim().isEmpty()) {
                user.setClassId(userRequest.getClassId().trim());
            } else {
                user.setClassId(null);
            }

            // 处理新字段
            user.setUserRole(userRequest.getUserRole());
            if (userRequest.getRealName() != null && !userRequest.getRealName().trim().isEmpty()) {
                user.setRealName(userRequest.getRealName().trim());
            } else {
                user.setRealName(null);
            }
            user.setGender(userRequest.getGender());
            // 注意：学号和教师工号在更新时通常不应该修改，但如果需要修改，可以在这里处理
            // 这里只允许设置，不允许清空（保持原有值）

            if (userService.updateById(user)) {
                // 返回更新后的用户信息
                User updatedUser = userService.getById(userId);
                UserResponse response = new UserResponse(updatedUser);
                return R.ok("用户信息更新成功").put("data", response);
            } else {
                return R.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return R.error("更新用户信息时发生错误: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public R getAllUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<User> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    @GetMapping("/list")
    public R getUsersByPage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionHeader,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        try {
            log.debug("=== Token调试信息 ===");
            log.debug("Authorization头: {}", authHeader);
            log.debug("X-Session-ID头: {}", sessionHeader);

            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader;
                log.debug("使用Authorization头");
            } else if (sessionHeader != null) {
                token = "Bearer " + sessionHeader;
                log.debug("使用X-Session-ID头");
            }

            log.debug("最终token: {}", token);

            if (token == null) {
                return R.error(401, "未提供token");
            }

            String userId = tokenUtils.extractUserIdFromToken(token);
            log.debug("解析出的用户ID: {}", userId);

            User currentUser = userService.getById(userId);
            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<User> users = userService.getUsersByPage(pageNum, pageSize);
            long total = userService.getUserCount();
            long totalPages = (total + pageSize - 1) / pageSize;

            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok()
                    .put("data", userResponses)
                    .put("pageNum", pageNum)
                    .put("pageSize", pageSize)
                    .put("total", total)
                    .put("totalPages", totalPages);
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return R.error(401, "未登录或token无效");
        }
    }

    @GetMapping("/role/{role}")
    public R getUsersByRole(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer role) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            if (role < 0 || role > 2) {
                return R.error(400, "角色参数无效（0-学生，1-教师，2-管理员）");
            }

            List<User> users = userService.getUsersByRole(role);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 新增用户
    @PostMapping("/add")
    public R addUser(@RequestHeader(value = "Authorization", required = false) String token,
                     @RequestBody UserRequest userRequest) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 手动验证请求参数
            String validationError = userRequest.validateForAdd();
            if (validationError != null) {
                return R.error(400, validationError);
            }

            User user = new User();
            user.setUserName(userRequest.getUserName().trim());
            user.setUserPhone(userRequest.getUserPhone() != null ? userRequest.getUserPhone().trim() : null);
            user.setUserAvatar(userRequest.getUserAvatar() != null ? userRequest.getUserAvatar().trim() : null);
            user.setUserRole(userRequest.getUserRole() != null ? userRequest.getUserRole() : 0);
            user.setCollegeId(userRequest.getCollegeId()); // 设置学院ID
            user.setClassId(userRequest.getClassId()); // 设置班级ID
            user.setPassword(userRequest.getPassword()); // 在service中会设置默认密码
            // 设置新字段
            user.setStudentNumber(userRequest.getStudentNumber() != null ? userRequest.getStudentNumber().trim() : null);
            user.setTeacherNumber(userRequest.getTeacherNumber() != null ? userRequest.getTeacherNumber().trim() : null);
            user.setRealName(userRequest.getRealName() != null ? userRequest.getRealName().trim() : null);
            user.setGender(userRequest.getGender());

            if (userService.addUser(user)) {
                UserResponse response = new UserResponse(user);
                return R.ok("用户添加成功").put("data", response);
            } else {
                return R.error("用户添加失败");
            }
        } catch (Exception e) {
            return R.error("添加用户时发生错误: " + e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/{userId}")
    public R deleteUser(@RequestHeader(value = "Authorization", required = false) String token,
                        @PathVariable String userId) {
        try {
            // 验证管理员权限
            String currentUserId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(currentUserId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 不能删除自己
            if (currentUserId.equals(userId)) {
                return R.error("不能删除当前登录的用户");
            }

            if (userService.deleteUser(userId)) {
                return R.ok("用户删除成功");
            } else {
                return R.error("用户删除失败");
            }
        } catch (Exception e) {
            return R.error("删除用户时发生错误: " + e.getMessage());
        }
    }

    // 搜索用户
    @GetMapping("/search")
    public R searchUsers(@RequestHeader(value = "Authorization", required = false) String token,
                         @RequestParam String keyword,
                         @RequestParam(defaultValue = "1") int pageNum,
                         @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            if (!StringUtils.hasText(keyword)) {
                return R.error(400, "搜索关键词不能为空");
            }

            List<User> users = userService.searchUsersByPage(keyword, pageNum, pageSize);
            long total = userService.getSearchCount(keyword);
            long totalPages = (total + pageSize - 1) / pageSize;

            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok()
                    .put("data", userResponses)
                    .put("pageNum", pageNum)
                    .put("pageSize", pageSize)
                    .put("total", total)
                    .put("totalPages", totalPages)
                    .put("keyword", keyword);
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 更改密码
    @PutMapping("/{userId}/password")
    public R changePassword(@RequestHeader(value = "Authorization", required = false) String token,
                            @PathVariable String userId,
                            @RequestBody Map<String, String> request) {
        try {
            String currentUserId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(currentUserId);

            if (currentUser == null) {
                return R.error(401, "未登录或token无效");
            }

            String newPassword = request.get("newPassword");
            if (!StringUtils.hasText(newPassword)) {
                return R.error(400, "新密码不能为空");
            }

            // 普通用户只能修改自己的密码，管理员可以修改任何用户的密码
            if (!currentUserId.equals(userId) && currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，只能修改自己的密码");
            }

            if (userService.changePassword(userId, newPassword)) {
                return R.ok("密码修改成功");
            } else {
                return R.error("密码修改失败");
            }
        } catch (Exception e) {
            return R.error("修改密码时发生错误: " + e.getMessage());
        }
    }

    // 获取用户详情（包含完整信息）
    @GetMapping("/{userId}/detail")
    public R getUserDetail(@RequestHeader(value = "Authorization", required = false) String token,
                           @PathVariable String userId) {
        try {
            // 验证管理员权限或用户查看自己的信息
            String currentUserId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(currentUserId);

            if (currentUser == null) {
                return R.error(401, "未登录或token无效");
            }

            // 普通用户只能查看自己的详情，管理员可以查看任何用户的详情
            if (!currentUserId.equals(userId) && currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足");
            }

            User user = userService.getUserDetail(userId);
            if (user != null) {
                UserResponse response = new UserResponse(user);
                return R.ok().put("data", response);
            } else {
                return R.error("用户不存在");
            }
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据学院获取用户列表（新增）
    @GetMapping("/college/{collegeId}")
    public R getUsersByCollege(@RequestHeader(value = "Authorization", required = false) String token,
                               @PathVariable String collegeId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<User> users = userService.getUsersByCollege(collegeId);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据学院和角色获取用户列表（新增）
    @GetMapping("/college/{collegeId}/role/{role}")
    public R getUsersByCollegeAndRole(@RequestHeader(value = "Authorization", required = false) String token,
                                      @PathVariable String collegeId,
                                      @PathVariable Integer role) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            if (role < 0 || role > 2) {
                return R.error(400, "角色参数无效（0-学生，1-教师，2-管理员）");
            }

            List<User> users = userService.getUsersByCollegeAndRole(collegeId, role);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据班级获取用户列表（新增）
    @GetMapping("/class/{classId}")
    public R getUsersByClass(@RequestHeader(value = "Authorization", required = false) String token,
                             @PathVariable String classId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<User> users = userService.getUsersByClass(classId);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据班级和角色获取用户列表（新增）
    @GetMapping("/class/{classId}/role/{role}")
    public R getUsersByClassAndRole(@RequestHeader(value = "Authorization", required = false) String token,
                                    @PathVariable String classId,
                                    @PathVariable Integer role) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            if (role < 0 || role > 2) {
                return R.error(400, "角色参数无效（0-学生，1-教师，2-管理员）");
            }

            List<User> users = userService.getUsersByClassAndRole(classId, role);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok().put("data", userResponses).put("total", users.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }
}