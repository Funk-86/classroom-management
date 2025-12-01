package org.example.classroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.Classroom;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ClassroomService extends IService<Classroom> {
    List<Classroom> getClassroomsByBuilding(String buildingId);
    Classroom getClassroomById(String id);
    IPage<Classroom> getAvailableClassrooms(LocalDate date, LocalTime startTime, LocalTime endTime,
                                            Integer minCapacity, String buildingId, String equipment,
                                            Integer page, Integer size);
    boolean updateClassroomStatus(String id, Integer status);
    IPage<Classroom> searchClassrooms(String keyword, Integer minCapacity, Integer maxCapacity,
                                      String equipment, Integer page, Integer size);
    List<Classroom> getPopularClassrooms(Integer limit);
    long count();
    boolean addClassroom(Classroom classroom);
    boolean updateClassroom(Classroom classroom);
    boolean deleteClassroom(String classroomId);
    List<Classroom> searchClassroomsByNameOrId(String keyword);
}