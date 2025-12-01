package org.example.classroom.controller;

import org.example.classroom.dto.ClassRequest;
import org.example.classroom.dto.ClassResponse;
import org.example.classroom.dto.R;
import org.example.classroom.entity.Class;
import org.example.classroom.entity.User;
import org.example.classroom.service.ClassService;
import org.example.classroom.service.UserService;
import org.example.classroom.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/class")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtils tokenUtils;

    // 添加班级
    @PostMapping("/add")
    public R addClass(@RequestHeader(value = "Authorization", required = false) String token,
                      @RequestBody ClassRequest classRequest) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 手动验证请求参数
            String validationError = classRequest.validate();
            if (validationError != null) {
                return R.error(400, validationError);
            }

            Class clazz = new Class();
            clazz.setClassCode(classRequest.getClassCode().trim());
            clazz.setClassName(classRequest.getClassName().trim());
            clazz.setCollegeId(classRequest.getCollegeId().trim());
            clazz.setCampusId(classRequest.getCampusId().trim());
            clazz.setGrade(classRequest.getGrade().trim());

            // 处理可选字段
            if (classRequest.getHeadTeacherId() != null && !classRequest.getHeadTeacherId().trim().isEmpty()) {
                clazz.setHeadTeacherId(classRequest.getHeadTeacherId().trim());
            }
            if (classRequest.getMajorName() != null && !classRequest.getMajorName().trim().isEmpty()) {
                clazz.setMajorName(classRequest.getMajorName().trim());
            }
            if (classRequest.getClassType() != null) {
                clazz.setClassType(classRequest.getClassType());
            }
            if (classRequest.getStatus() != null) {
                clazz.setStatus(classRequest.getStatus());
            }
            clazz.setStartDate(classRequest.getStartDate());
            clazz.setEndDate(classRequest.getEndDate());

            if (classService.addClass(clazz)) {
                ClassResponse response = new ClassResponse(clazz);
                response.setStudentCount(0); // 新班级学生数为0
                return R.ok("班级添加成功").put("data", response);
            } else {
                return R.error("班级添加失败");
            }
        } catch (Exception e) {
            return R.error("添加班级时发生错误: " + e.getMessage());
        }
    }

    // 更新班级信息
    @PutMapping("/{classId}")
    public R updateClass(@RequestHeader(value = "Authorization", required = false) String token,
                         @PathVariable String classId,
                         @RequestBody ClassRequest classRequest) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 手动验证请求参数
            String validationError = classRequest.validate();
            if (validationError != null) {
                return R.error(400, validationError);
            }

            Class existingClass = classService.getClassById(classId);
            if (existingClass == null) {
                return R.error("班级不存在");
            }

            Class clazz = new Class();
            clazz.setClassId(classId);
            clazz.setClassCode(classRequest.getClassCode().trim());
            clazz.setClassName(classRequest.getClassName().trim());
            clazz.setCollegeId(classRequest.getCollegeId().trim());
            clazz.setCampusId(classRequest.getCampusId().trim());
            clazz.setGrade(classRequest.getGrade().trim());

            // 处理可选字段
            if (classRequest.getHeadTeacherId() != null && !classRequest.getHeadTeacherId().trim().isEmpty()) {
                clazz.setHeadTeacherId(classRequest.getHeadTeacherId().trim());
            } else {
                clazz.setHeadTeacherId(null);
            }
            if (classRequest.getMajorName() != null && !classRequest.getMajorName().trim().isEmpty()) {
                clazz.setMajorName(classRequest.getMajorName().trim());
            } else {
                clazz.setMajorName(null);
            }
            if (classRequest.getClassType() != null) {
                clazz.setClassType(classRequest.getClassType());
            }
            if (classRequest.getStatus() != null) {
                clazz.setStatus(classRequest.getStatus());
            }
            clazz.setStartDate(classRequest.getStartDate());
            clazz.setEndDate(classRequest.getEndDate());

            if (classService.updateClass(clazz)) {
                // 返回更新后的班级信息
                Class updatedClass = classService.getClassDetail(classId);
                ClassResponse response = new ClassResponse(updatedClass);
                response.setStudentCount(classService.countStudentsByClass(classId));
                return R.ok("班级信息更新成功").put("data", response);
            } else {
                return R.error("班级信息更新失败");
            }
        } catch (Exception e) {
            return R.error("更新班级信息时发生错误: " + e.getMessage());
        }
    }

    // 删除班级
    @DeleteMapping("/{classId}")
    public R deleteClass(@RequestHeader(value = "Authorization", required = false) String token,
                         @PathVariable String classId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            if (classService.deleteClass(classId)) {
                return R.ok("班级删除成功");
            } else {
                return R.error("班级删除失败");
            }
        } catch (Exception e) {
            return R.error("删除班级时发生错误: " + e.getMessage());
        }
    }

    // 根据ID获取班级信息
    @GetMapping("/{classId}")
    public R getClassById(@PathVariable String classId) {
        Class clazz = classService.getClassDetail(classId);
        if (clazz != null) {
            ClassResponse response = new ClassResponse(clazz);
            response.setStudentCount(classService.countStudentsByClass(classId));
            return R.ok().put("data", response);
        } else {
            return R.error("班级不存在");
        }
    }

    // 获取所有班级列表
    @GetMapping("/all")
    public R getAllClasses(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<Class> classes = classService.getAllClasses();
            List<ClassResponse> classResponses = classes.stream()
                    .map(clazz -> {
                        ClassResponse response = new ClassResponse(clazz);
                        response.setStudentCount(classService.countStudentsByClass(clazz.getClassId()));
                        return response;
                    })
                    .collect(Collectors.toList());

            return R.ok().put("data", classResponses).put("total", classes.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 分页查询班级列表
    @GetMapping("/list")
    public R getClassesByPage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<Class> classes = classService.getClassesByPage(pageNum, pageSize);
            long total = classService.getClassCount();
            long totalPages = (total + pageSize - 1) / pageSize;

            List<ClassResponse> classResponses = classes.stream()
                    .map(clazz -> {
                        ClassResponse response = new ClassResponse(clazz);
                        response.setStudentCount(classService.countStudentsByClass(clazz.getClassId()));
                        return response;
                    })
                    .collect(Collectors.toList());

            return R.ok()
                    .put("data", classResponses)
                    .put("pageNum", pageNum)
                    .put("pageSize", pageSize)
                    .put("total", total)
                    .put("totalPages", totalPages);
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 搜索班级
    @GetMapping("/search")
    public R searchClasses(@RequestHeader(value = "Authorization", required = false) String token,
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

            List<Class> classes = classService.searchClassesByPage(keyword, pageNum, pageSize);
            long total = classService.getSearchCount(keyword);
            long totalPages = (total + pageSize - 1) / pageSize;

            List<ClassResponse> classResponses = classes.stream()
                    .map(clazz -> {
                        ClassResponse response = new ClassResponse(clazz);
                        response.setStudentCount(classService.countStudentsByClass(clazz.getClassId()));
                        return response;
                    })
                    .collect(Collectors.toList());

            return R.ok()
                    .put("data", classResponses)
                    .put("pageNum", pageNum)
                    .put("pageSize", pageSize)
                    .put("total", total)
                    .put("totalPages", totalPages)
                    .put("keyword", keyword);
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据学院获取班级列表
    @GetMapping("/college/{collegeId}")
    public R getClassesByCollege(@RequestHeader(value = "Authorization", required = false) String token,
                                 @PathVariable String collegeId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<Class> classes = classService.getClassesByCollege(collegeId);
            List<ClassResponse> classResponses = classes.stream()
                    .map(clazz -> {
                        ClassResponse response = new ClassResponse(clazz);
                        response.setStudentCount(classService.countStudentsByClass(clazz.getClassId()));
                        return response;
                    })
                    .collect(Collectors.toList());

            return R.ok().put("data", classResponses).put("total", classes.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 根据校区获取班级列表
    @GetMapping("/campus/{campusId}")
    public R getClassesByCampus(@RequestHeader(value = "Authorization", required = false) String token,
                                @PathVariable String campusId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<Class> classes = classService.getClassesByCampus(campusId);
            List<ClassResponse> classResponses = classes.stream()
                    .map(clazz -> {
                        ClassResponse response = new ClassResponse(clazz);
                        response.setStudentCount(classService.countStudentsByClass(clazz.getClassId()));
                        return response;
                    })
                    .collect(Collectors.toList());

            return R.ok().put("data", classResponses).put("total", classes.size());
        } catch (Exception e) {
            return R.error(401, "未登录或token无效");
        }
    }

    // 获取班级中的学生列表
    @GetMapping("/{classId}/students")
    public R getStudentsByClass(@RequestHeader(value = "Authorization", required = false) String token,
                                @PathVariable String classId,
                                @RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 检查班级是否存在
            Class clazz = classService.getClassById(classId);
            if (clazz == null) {
                return R.error("班级不存在");
            }

            // 使用UserService查询该班级的学生
            List<User> students = userService.getUsersByClass(classId);
            int total = classService.countStudentsByClass(classId);
            long totalPages = (total + pageSize - 1) / pageSize;

            // 分页处理
            int fromIndex = (pageNum - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, students.size());
            List<User> pagedStudents = students.subList(fromIndex, toIndex);

            // 转换为UserResponse
            List<org.example.classroom.dto.UserResponse> studentResponses = pagedStudents.stream()
                    .map(org.example.classroom.dto.UserResponse::new)
                    .collect(Collectors.toList());

            return R.ok()
                    .put("data", studentResponses)
                    .put("classInfo", new ClassResponse(clazz))
                    .put("pageNum", pageNum)
                    .put("pageSize", pageSize)
                    .put("total", total)
                    .put("totalPages", totalPages);
        } catch (Exception e) {
            return R.error("获取班级学生列表时发生错误: " + e.getMessage());
        }
    }

    // 统计班级学生人数
    @GetMapping("/{classId}/student-count")
    public R getStudentCountByClass(@RequestHeader(value = "Authorization", required = false) String token,
                                    @PathVariable String classId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            // 检查班级是否存在
            Class clazz = classService.getClassById(classId);
            if (clazz == null) {
                return R.error("班级不存在");
            }

            int studentCount = classService.countStudentsByClass(classId);

            return R.ok()
                    .put("classId", classId)
                    .put("className", clazz.getClassName())
                    .put("studentCount", studentCount);
        } catch (Exception e) {
            return R.error("统计班级学生人数时发生错误: " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public R getClassesByCollegeAndCampus(@RequestHeader(value = "Authorization", required = false) String token,
                                          @RequestParam(required = false) String collegeId,
                                          @RequestParam(required = false) String campusId) {
        try {
            // 验证管理员权限
            String userId = tokenUtils.extractUserIdFromToken(token);
            User currentUser = userService.getById(userId);

            if (currentUser == null || currentUser.getUserRole() != 2) {
                return R.error(403, "权限不足，需要管理员权限");
            }

            List<Map<String, Object>> classes = classService.getClassesByCollege(collegeId, campusId);

            return R.ok()
                    .put("data", classes)
                    .put("total", classes.size())
                    .put("collegeId", collegeId)
                    .put("campusId", campusId);
        } catch (Exception e) {
            return R.error("获取班级列表时发生错误: " + e.getMessage());
        }
    }
}