package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.Class;
import org.example.classroom.mapper.ClassMapper;
import org.example.classroom.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Override
    public boolean addClass(Class clazz) {
        if (clazz == null || !StringUtils.hasText(clazz.getClassCode())) {
            return false;
        }

        // 检查班级代码是否已存在
        if (isClassCodeExists(clazz.getClassCode())) {
            throw new RuntimeException("班级代码已存在");
        }

        // 设置默认值
        if (clazz.getClassType() == null) {
            clazz.setClassType(0); // 默认普通班
        }
        if (clazz.getStatus() == null) {
            clazz.setStatus(1); // 默认正常状态
        }

        return save(clazz);
    }

    @Override
    public boolean updateClass(Class clazz) {
        if (clazz == null || !StringUtils.hasText(clazz.getClassId())) {
            return false;
        }

        // 检查班级代码是否与其他班级冲突
        Class existingClass = classMapper.selectByClassCode(clazz.getClassCode());
        if (existingClass != null && !existingClass.getClassId().equals(clazz.getClassId())) {
            throw new RuntimeException("班级代码已存在");
        }

        return updateById(clazz);
    }

    @Override
    public boolean deleteClass(String classId) {
        if (!StringUtils.hasText(classId)) {
            return false;
        }

        // 检查班级中是否有学生
        int studentCount = countStudentsByClass(classId);
        if (studentCount > 0) {
            throw new RuntimeException("班级中还有学生，无法删除");
        }

        return removeById(classId);
    }

    @Override
    public Class getClassById(String classId) {
        if (!StringUtils.hasText(classId)) {
            return null;
        }
        return getById(classId);
    }

    @Override
    public List<Class> getAllClasses() {
        return classMapper.selectAllClassesWithInfo();
    }

    @Override
    public List<Class> getClassesByPage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        int offset = (pageNum - 1) * pageSize;
        return classMapper.selectClassesByPage(offset, pageSize);
    }

    @Override
    public List<Class> getClassesByCollege(String collegeId) {
        if (!StringUtils.hasText(collegeId)) {
            return getAllClasses();
        }
        return classMapper.selectClassesByCollege(collegeId);
    }

    @Override
    public List<Class> getClassesByCampus(String campusId) {
        if (!StringUtils.hasText(campusId)) {
            return getAllClasses();
        }
        return classMapper.selectClassesByCampus(campusId);
    }

    @Override
    public List<Class> searchClasses(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllClasses();
        }
        return classMapper.searchClasses(keyword.trim());
    }

    @Override
    public List<Class> searchClassesByPage(String keyword, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        int offset = (pageNum - 1) * pageSize;

        if (!StringUtils.hasText(keyword)) {
            return getClassesByPage(pageNum, pageSize);
        }

        return classMapper.searchClassesByPage(keyword.trim(), offset, pageSize);
    }

    @Override
    public long getClassCount() {
        return count();
    }

    @Override
    public long getSearchCount(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getClassCount();
        }
        return classMapper.countByKeyword(keyword.trim());
    }

    @Override
    public int countStudentsByClass(String classId) {
        if (!StringUtils.hasText(classId)) {
            return 0;
        }
        return classMapper.countStudentsByClass(classId);
    }

    @Override
    public Class getClassDetail(String classId) {
        if (!StringUtils.hasText(classId)) {
            return null;
        }
        return classMapper.selectClassDetail(classId);
    }

    @Override
    public boolean isClassCodeExists(String classCode) {
        if (!StringUtils.hasText(classCode)) {
            return false;
        }
        Class existingClass = classMapper.selectByClassCode(classCode);
        return existingClass != null;
    }

    @Override
    public List<Map<String, Object>> getClassesByCollege(String collegeId, String campusId) {
        // 使用自定义Mapper查询
        List<Class> classes = classMapper.selectClassesByCollegeAndCampus(collegeId, campusId);

        // 转换为Map格式返回
        List<Map<String, Object>> result = new ArrayList<>();

        for (Class clazz : classes) {
            Map<String, Object> classMap = new HashMap<>();
            classMap.put("classId", clazz.getClassId());
            classMap.put("classCode", clazz.getClassCode());
            classMap.put("className", clazz.getClassName());
            classMap.put("collegeId", clazz.getCollegeId());
            classMap.put("campusId", clazz.getCampusId());
            classMap.put("grade", clazz.getGrade());
            classMap.put("majorName", clazz.getMajorName());
            classMap.put("classType", clazz.getClassType());
            classMap.put("status", clazz.getStatus());
            classMap.put("startDate", clazz.getStartDate());
            classMap.put("endDate", clazz.getEndDate());
            classMap.put("createdAt", clazz.getCreatedAt());
            classMap.put("updatedAt", clazz.getUpdatedAt());

            // 统计学生人数
            int studentCount = countStudentsByClass(clazz.getClassId());
            classMap.put("studentCount", studentCount);

            result.add(classMap);
        }

        return result;
    }

}