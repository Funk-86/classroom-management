package org.example.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.Class;
import java.util.List;
import java.util.Map;

public interface ClassService extends IService<Class> {

    // 添加班级
    boolean addClass(Class clazz);

    // 更新班级
    boolean updateClass(Class clazz);

    // 删除班级
    boolean deleteClass(String classId);

    // 根据ID查询班级
    Class getClassById(String classId);

    // 查询所有班级
    List<Class> getAllClasses();

    // 分页查询班级
    List<Class> getClassesByPage(int pageNum, int pageSize);

    // 根据学院查询班级
    List<Class> getClassesByCollege(String collegeId);

    // 根据校区查询班级
    List<Class> getClassesByCampus(String campusId);

    // 搜索班级
    List<Class> searchClasses(String keyword);

    // 分页搜索班级
    List<Class> searchClassesByPage(String keyword, int pageNum, int pageSize);

    // 获取班级总数
    long getClassCount();

    // 获取搜索数量
    long getSearchCount(String keyword);

    // 统计班级学生人数
    int countStudentsByClass(String classId);

    // 获取班级详情
    Class getClassDetail(String classId);

    // 检查班级代码是否已存在
    boolean isClassCodeExists(String classCode);

    List<Map<String, Object>> getClassesByCollege(String collegeId, String campusId);
}