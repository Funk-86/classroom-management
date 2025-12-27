package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.entity.Classroom;
import org.example.classroom.mapper.ClassroomMapper;
import org.example.classroom.service.ClassroomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {

    @Override
    public List<Classroom> getClassroomsByBuilding(String buildingId) {
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("building_id", buildingId)
                .orderByAsc("floor_num")
                .orderByAsc("classroom_name");
        return list(queryWrapper);
    }

    @Override
    public Classroom getClassroomById(String id) {
        return getById(id);
    }

    @Override
    public IPage<Classroom> getAvailableClassrooms(LocalDate date, LocalTime startTime, LocalTime endTime,
                                                   Integer minCapacity, String buildingId, String equipment,
                                                   Integer page, Integer size) {
        Page<Classroom> pageParam = new Page<>(page, size);
        return baseMapper.selectAvailableClassrooms(pageParam, date, startTime, endTime,
                minCapacity, buildingId, equipment);
    }

    @Override
    public boolean updateClassroomStatus(String id, Integer status) {
        Classroom classroom = getById(id);
        if (classroom != null) {
            classroom.setStatus(status);
            return updateById(classroom);
        }
        return false;
    }

    @Override
    public IPage<Classroom> searchClassrooms(String keyword, Integer minCapacity, Integer maxCapacity,
                                             String equipment, Integer page, Integer size) {
        Page<Classroom> pageParam = new Page<>(page, size);
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("classroom_name", keyword)
                    .or()
                    .like("classroom_id", keyword);
        }

        if (minCapacity != null) {
            queryWrapper.ge("capacity", minCapacity);
        }

        if (maxCapacity != null) {
            queryWrapper.le("capacity", maxCapacity);
        }

        if (equipment != null && !equipment.trim().isEmpty()) {
            queryWrapper.like("equipment", equipment);
        }

        queryWrapper.orderByAsc("building_id")
                .orderByAsc("floor_num")
                .orderByAsc("classroom_name");

        return page(pageParam, queryWrapper);
    }

    @Override
    public List<Classroom> getPopularClassrooms(Integer limit) {
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("capacity") // 暂时按容量排序，实际应该按预约次数
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public long count() {
        return baseMapper.selectCount(null);
    }

    @Override
    public boolean addClassroom(Classroom classroom) {
        return save(classroom);
    }

    @Override
    public boolean updateClassroom(Classroom classroom) {
        return updateById(classroom);
    }

    @Override
    public boolean deleteClassroom(String classroomId) {
        if (classroomId == null || classroomId.trim().isEmpty()) {
            throw new IllegalArgumentException("教室ID不能为空");
        }

        // 检查是否有关联的预约记录
        long reservationCount = baseMapper.countReservationsByClassroomId(classroomId);
        if (reservationCount > 0) {
            throw new RuntimeException("该教室存在 " + reservationCount + " 条预约记录，无法删除。请先删除或处理相关预约记录。");
        }

        // 检查是否有关联的课程安排
        long scheduleCount = baseMapper.countCourseSchedulesByClassroomId(classroomId);
        if (scheduleCount > 0) {
            throw new RuntimeException("该教室存在 " + scheduleCount + " 条课程安排，无法删除。请先删除或处理相关课程安排。");
        }

        return removeById(classroomId);
    }

    @Override
    public List<Classroom> searchClassroomsByNameOrId(String keyword) {
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("classroom_name", keyword)
                    .or()
                    .like("classroom_id", keyword);
        }
        queryWrapper.orderByAsc("building_id")
                .orderByAsc("floor_num")
                .orderByAsc("classroom_name");
        return list(queryWrapper);
    }
}