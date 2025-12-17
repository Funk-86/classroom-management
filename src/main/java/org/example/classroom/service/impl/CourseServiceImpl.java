package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.classroom.controller.CourseController;
import org.example.classroom.entity.Course;
import org.example.classroom.entity.CourseSchedule;
import org.example.classroom.entity.StudentCourse;
import org.example.classroom.entity.User;
import org.example.classroom.mapper.CourseMapper;
import org.example.classroom.mapper.CourseScheduleMapper;
import org.example.classroom.mapper.StudentCourseMapper;
import org.example.classroom.mapper.UserMapper;
import org.example.classroom.mapper.CourseClassMapper;
import org.example.classroom.entity.CourseClass;
import org.example.classroom.service.CourseService;
import org.example.classroom.util.WeekCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseScheduleMapper courseScheduleMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Autowired
    private CourseClassMapper courseClassMapper;

    @Override
    public IPage<Course> getCoursesWithDetail(String collegeId, String academicYear, Integer semester,
                                              Integer courseType, String teacherId, String studentId,
                                              Integer enrollmentStatus, Integer isRequired, String keyword,
                                              Integer page, Integer size) {
        Page<Course> pageParam = new Page<>(page, size);
        return courseMapper.selectCoursesWithDetail(pageParam, collegeId, academicYear, semester,
                courseType, teacherId, studentId, enrollmentStatus, isRequired, keyword);
    }

    @Override
    public Course getCourseWithDetail(String courseId) {
        Course course = courseMapper.selectCourseWithDetail(courseId);
        if (course != null) {
            // 查询课程关联的所有班级
            List<CourseClass> courseClasses = courseClassMapper.selectByCourseId(courseId);
            if (courseClasses != null && !courseClasses.isEmpty()) {
                // 设置多个班级信息
                List<String> classNames = new java.util.ArrayList<>();
                List<String> classCodes = new java.util.ArrayList<>();
                for (CourseClass cc : courseClasses) {
                    if (cc.getClassName() != null) {
                        classNames.add(cc.getClassName());
                    }
                    if (cc.getClassCode() != null) {
                        classCodes.add(cc.getClassCode());
                    }
                }
                course.setClassNames(classNames);
                course.setClassCodes(classCodes);

                // 兼容旧数据：如果只有一个班级，也设置单个班级名称
                if (classNames.size() == 1) {
                    course.setClassName(classNames.get(0));
                } else if (classNames.size() > 1) {
                    // 多个班级时，用逗号连接作为单个显示
                    course.setClassName(String.join(", ", classNames));
                }
            }
        }
        return course;
    }

    @Override
    public List<Course> getCoursesByTeacher(String teacherId, String academicYear, Integer semester) {
        return courseMapper.selectCoursesByTeacher(teacherId, academicYear, semester);
    }

    @Override
    public boolean addCourse(Course course) {
        // 检查课程代码是否重复
        int count = courseMapper.checkCourseExists(course.getCourseCode(),
                course.getAcademicYear(),
                course.getSemester());
        if (count > 0) {
            throw new RuntimeException("该课程代码在当前学年学期已存在");
        }

        // 设置默认值
        if (course.getEnrollmentStatus() == null) {
            course.setEnrollmentStatus(0); // 默认未选
        }
        if (course.getIsRequired() == null) {
            course.setIsRequired(0); // 默认选修
        }
        // 如果分配了班级，设置班级相关字段
        if (course.getClassId() != null) {
            course.setIsRequired(1); // 班级课程默认为必修
        }

        return save(course);
    }


    @Override
    public boolean updateCourse(Course course) {
        return updateById(course);
    }

    @Override
    public boolean deleteCourse(String courseId) {
        Course course = getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查是否有学生选课记录
        QueryWrapper<StudentCourse> scWrapper = new QueryWrapper<>();
        scWrapper.eq("course_id", courseId);
        Long scCount = studentCourseMapper.selectCount(scWrapper);
        if (scCount > 0) {
            throw new RuntimeException("该课程存在学生选课记录，无法删除");
        }

        // 检查是否有课程安排
        QueryWrapper<CourseSchedule> scheduleWrapper = new QueryWrapper<>();
        scheduleWrapper.eq("course_id", courseId);
        Long scheduleCount = courseScheduleMapper.selectCount(scheduleWrapper);
        if (scheduleCount > 0) {
            throw new RuntimeException("该课程存在课程安排，无法删除");
        }

        return removeById(courseId);
    }

    @Override
    public IPage<CourseSchedule> getSchedulesWithDetail(String courseId, String classroomId, String campusId,
                                                        Integer dayOfWeek, Integer scheduleType,
                                                        LocalDate startDate, LocalDate endDate,
                                                        Integer page, Integer size) {
        Page<CourseSchedule> pageParam = new Page<>(page, size);
        return courseScheduleMapper.selectSchedulesWithDetail(pageParam, courseId, classroomId, campusId,
                dayOfWeek, scheduleType, startDate, endDate);
    }

    @Override
    public CourseSchedule getScheduleWithDetail(String scheduleId) {
        return courseScheduleMapper.selectScheduleWithDetail(scheduleId);
    }

    @Autowired
    private org.example.classroom.service.ClassroomOccupationService classroomOccupationService;

    @Override
    public boolean addCourseSchedule(CourseSchedule schedule) {
        // 使用统一的教室占用冲突检测
        java.time.LocalDate checkDate = schedule.getScheduleDate() != null
                ? schedule.getScheduleDate()
                : java.time.LocalDate.now();

        org.example.classroom.dto.ClassroomConflictResult conflictResult =
                classroomOccupationService.checkClassroomOccupation(
                        schedule.getClassroomId(),
                        checkDate,
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        null,
                        null // 新建课程安排，无需排除
                );

        if (conflictResult.isHasConflict()) {
            throw new RuntimeException("该时间段教室已被占用: " + conflictResult.getMessage());
        }
        return courseScheduleMapper.insert(schedule) > 0;
    }

    @Override
    public boolean updateCourseSchedule(CourseSchedule schedule) {
        // 使用统一的教室占用冲突检测（排除自身）
        java.time.LocalDate checkDate = schedule.getScheduleDate() != null
                ? schedule.getScheduleDate()
                : java.time.LocalDate.now();

        org.example.classroom.dto.ClassroomConflictResult conflictResult =
                classroomOccupationService.checkClassroomOccupation(
                        schedule.getClassroomId(),
                        checkDate,
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        null,
                        schedule.getScheduleId() // 更新时排除自身
                );

        if (conflictResult.isHasConflict()) {
            throw new RuntimeException("该时间段教室已被占用: " + conflictResult.getMessage());
        }
        return courseScheduleMapper.updateById(schedule) > 0;
    }

    @Override
    public boolean deleteCourseSchedule(String scheduleId) {
        return courseScheduleMapper.deleteById(scheduleId) > 0;
    }

    @Override
    public List<CourseSchedule> getStudentTimetable(String studentId, LocalDate date) {
        // 需要先通过学生ID获取学院，再获取课程安排
        // 这里简化实现，实际需要复杂的SQL查询
        QueryWrapper<CourseSchedule> wrapper = new QueryWrapper<>();
        // 实际实现需要关联多个表查询学生的课表
        return courseScheduleMapper.selectList(wrapper);
    }

    @Override
    public List<CourseSchedule> getTeacherTimetable(String teacherId, LocalDate date) {
        QueryWrapper<CourseSchedule> wrapper = new QueryWrapper<>();
        wrapper.inSql("course_id",
                String.format("SELECT course_id FROM courses WHERE teacher_id = '%s'", teacherId));

        if (date != null) {
            wrapper.and(w -> w.eq("schedule_type", 1).eq("schedule_date", date)
                    .or(w1 -> w1.eq("schedule_type", 0).apply("DAYOFWEEK('{0}') = day_of_week", date)));
        }

        wrapper.orderByAsc("day_of_week").orderByAsc("start_time");
        return courseScheduleMapper.selectList(wrapper);
    }

    @Override
    public boolean checkScheduleConflict(CourseSchedule schedule) {
        // 转换日期格式
        Date checkDate = schedule.getScheduleDate() != null
                ? Date.from(schedule.getScheduleDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                : new Date();

        // 转换时间格式 - 使用java.sql.Time
        Time startTime = Time.valueOf(schedule.getStartTime());
        Time endTime = Time.valueOf(schedule.getEndTime());

        List<CourseSchedule> conflicts = courseScheduleMapper.checkClassroomConflict(
                schedule.getClassroomId(),
                checkDate,
                startTime,
                endTime
        );

        // 排除自身
        if (schedule.getScheduleId() != null) {
            conflicts.removeIf(conflict -> conflict.getScheduleId().equals(schedule.getScheduleId()));
        }

        return !conflicts.isEmpty();
    }

    @Override
    public IPage<StudentCourse> getStudentCoursesWithDetail(String studentId, String courseId, String collegeId,
                                                            String academicYear, Integer semester,
                                                            Integer enrollmentStatus, Integer page, Integer size) {
        Page<StudentCourse> pageParam = new Page<>(page, size);
        return studentCourseMapper.selectStudentCoursesWithDetail(pageParam, studentId, courseId,
                collegeId, academicYear, semester, enrollmentStatus);
    }

    @Override
    @Transactional
    public boolean enrollCourse(StudentCourse studentCourse) {
        // 检查是否已经选过该课程
        boolean alreadyEnrolled = studentCourseMapper.checkCourseEnrollment(
                studentCourse.getStudentId(),
                studentCourse.getCourseId(),
                studentCourse.getAcademicYear(),
                studentCourse.getSemester()) > 0;

        if (alreadyEnrolled) {
            throw new RuntimeException("该课程已经选过，不能重复选课");
        }

        // 检查课程是否存在且为选修课
        Course course = getById(studentCourse.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        if (course.getCourseType() != 1) { // 不是选修课
            throw new RuntimeException("只能选择选修课程");
        }

        // 设置选课日期
        studentCourse.setEnrollmentDate(java.time.LocalDate.now());
        studentCourse.setIsAssigned(0); // 学生自选

        return studentCourseMapper.insert(studentCourse) > 0;
    }


    @Override
    @Transactional
    public boolean withdrawCourse(String studentCourseId) {
        StudentCourse studentCourse = studentCourseMapper.selectById(studentCourseId);
        if (studentCourse == null) {
            throw new RuntimeException("选课记录不存在");
        }

        // 检查是否是管理员分配的必修课
        if (studentCourse.getIsAssigned() != null && studentCourse.getIsAssigned() == 1) {
            throw new RuntimeException("管理员分配的必修课不能退选");
        }

        studentCourse.setEnrollmentStatus(2); // 退选状态
        return studentCourseMapper.updateById(studentCourse) > 0;
    }


    @Override
    @Transactional
    public boolean updateCourseScore(String studentCourseId, BigDecimal score) {
        StudentCourse studentCourse = studentCourseMapper.selectById(studentCourseId);
        if (studentCourse == null) {
            throw new RuntimeException("选课记录不存在");
        }

        studentCourse.setScore(score);
        studentCourse.setEnrollmentStatus(3); // 完成状态
        return studentCourseMapper.updateById(studentCourse) > 0;
    }

    @Override
    public List<StudentCourse> getStudentCourseList(String studentId, String academicYear, Integer semester) {
        return studentCourseMapper.selectStudentCourses(studentId, academicYear, semester);
    }

    @Override
    public List<Course> getAllStudentCourses(String studentId, String classId, String academicYear, Integer semester) {
        // 合并课程列表，去重
        java.util.Map<String, Course> allCoursesMap = new java.util.HashMap<>();

        System.out.println("--- getAllStudentCourses 开始 ---");
        System.out.println("学生ID: " + studentId + ", 班级ID: " + classId + ", 学年: " + academicYear + ", 学期: " + semester);

        // 1. 获取学生已选的课程
        List<StudentCourse> enrolledCourses = studentCourseMapper.selectStudentCourses(studentId, academicYear, semester);
        System.out.println("已选课程数量: " + (enrolledCourses != null ? enrolledCourses.size() : 0));
        for (StudentCourse sc : enrolledCourses) {
            if (sc.getCourseId() != null) {
                Course course = getCourseWithDetail(sc.getCourseId());
                if (course != null) {
                    allCoursesMap.put(course.getCourseId(), course);
                    System.out.println("已选课程: " + course.getCourseName() + " (ID: " + course.getCourseId() + ")");
                }
            }
        }

        // 2. 获取班级课程
        if (classId != null && !classId.trim().isEmpty()) {
            // 先查询班级课程的基本信息（不限制academicYear和semester，如果参数为空）
            QueryWrapper<Course> classCourseWrapper = new QueryWrapper<>();
            classCourseWrapper.eq("class_id", classId);
            // 只有当参数不为空时才添加条件
            if (academicYear != null && !academicYear.trim().isEmpty()) {
                classCourseWrapper.eq("academic_year", academicYear);
            }
            // 只有当参数不为空时才添加条件
            if (semester != null) {
                classCourseWrapper.eq("semester", semester);
            }

            System.out.println("查询班级课程的SQL条件: classId=" + classId + ", academicYear=" + academicYear + ", semester=" + semester);
            List<Course> classCoursesBasic = courseMapper.selectList(classCourseWrapper);
            System.out.println("查询到的班级课程数量: " + (classCoursesBasic != null ? classCoursesBasic.size() : 0));

            // 如果按条件查询不到，尝试只按班级ID查询（不限制学年学期）
            if ((classCoursesBasic == null || classCoursesBasic.isEmpty()) &&
                    (academicYear != null || semester != null)) {
                System.out.println("按条件未查询到课程，尝试只按班级ID查询...");
                QueryWrapper<Course> fallbackWrapper = new QueryWrapper<>();
                fallbackWrapper.eq("class_id", classId);
                classCoursesBasic = courseMapper.selectList(fallbackWrapper);
                System.out.println("只按班级ID查询到的课程数量: " + (classCoursesBasic != null ? classCoursesBasic.size() : 0));
            }

            // 获取每个班级课程的完整信息（包括teacherName等关联字段）
            for (Course basicCourse : classCoursesBasic) {
                if (basicCourse != null && basicCourse.getCourseId() != null) {
                    System.out.println("班级课程: " + basicCourse.getCourseName() + " (ID: " + basicCourse.getCourseId() +
                            ", 学年: " + basicCourse.getAcademicYear() + ", 学期: " + basicCourse.getSemester() + ")");
                    Course fullCourse = getCourseWithDetail(basicCourse.getCourseId());
                    if (fullCourse != null) {
                        allCoursesMap.put(fullCourse.getCourseId(), fullCourse);
                    }
                }
            }
        } else {
            System.out.println("班级ID为空，跳过班级课程查询");
        }

        System.out.println("最终返回的课程数量: " + allCoursesMap.size());
        System.out.println("--- getAllStudentCourses 结束 ---");
        return new java.util.ArrayList<>(allCoursesMap.values());
    }

    @Override
    public boolean checkCourseEnrollment(String studentId, String courseId, String academicYear, Integer semester) {
        return studentCourseMapper.checkCourseEnrollment(studentId, courseId, academicYear, semester) > 0;
    }
    @Override
    public List<Course> getAvailableCoursesForStudent(String studentId, String academicYear, Integer semester) {
        return courseMapper.selectAvailableCoursesForStudent(studentId, academicYear, semester);
    }

    @Override
    public List<Course> getRequiredCoursesForStudent(String studentId, String academicYear, Integer semester) {
        return courseMapper.selectRequiredCoursesForStudent(studentId, academicYear, semester);
    }

    @Override
    @Transactional
    public boolean assignCourseToStudent(String courseId, String studentId, Integer isRequired, String assignerId) {
        Course course = getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查是否已经分配或选过
        boolean alreadyExists = studentCourseMapper.checkCourseEnrollment(
                studentId, courseId, course.getAcademicYear(), course.getSemester()) > 0;

        if (alreadyExists) {
            throw new RuntimeException("该课程已经分配或选过");
        }

        // 创建选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        studentCourse.setCollegeId(course.getCollegeId());
        studentCourse.setAcademicYear(course.getAcademicYear());
        studentCourse.setSemester(course.getSemester());
        studentCourse.setEnrollmentStatus(1); // 已选状态
        studentCourse.setIsAssigned(1); // 管理员分配
        studentCourse.setAssignedBy(assignerId);
        studentCourse.setAssignedAt(LocalDateTime.now());
        studentCourse.setEnrollmentDate(java.time.LocalDate.now());

        boolean success = studentCourseMapper.insert(studentCourse) > 0;

        if (success) {
            // 同时更新课程表中的学生信息（如果课程是分配给特定学生的）
            if (isRequired != null && isRequired == 1) {
                course.setStudentId(studentId);
                course.setEnrollmentStatus(1);
                course.setIsRequired(1);
                course.setAssignedBy(assignerId);
                course.setAssignedAt(LocalDateTime.now());
                updateById(course);
            }
        }

        return success;
    }

    @Override
    @Transactional
    public int batchAssignCourses(List<CourseController.BatchAssignRequest> requests, String assignerId) {
        int successCount = 0;
        for (CourseController.BatchAssignRequest request : requests) {
            try {
                if (assignCourseToStudent(request.getCourseId(), request.getStudentId(),
                        request.getIsRequired(), assignerId)) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他分配
                System.err.println("分配课程失败: " + e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public boolean canStudentEnrollCourse(String studentId, String courseId) {
        Course course = getById(courseId);
        if (course == null) {
            return false;
        }

        // 检查是否已经选过
        boolean alreadyEnrolled = studentCourseMapper.checkCourseEnrollment(
                studentId, courseId, course.getAcademicYear(), course.getSemester()) > 0;

        if (alreadyEnrolled) {
            return false;
        }

        // 检查课程类型和容量限制等
        // 这里可以添加更多的业务逻辑检查

        return true;
    }

    @Override
    @Transactional
    public boolean assignCourseToClass(String courseId, String classId, String assignerId) {
        Course course = getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查关联是否已存在
        if (courseClassMapper.checkExists(courseId, classId) > 0) {
            throw new RuntimeException("该课程已分配给该班级");
        }

        // 创建课程-班级关联
        CourseClass courseClass = new CourseClass();
        courseClass.setCourseId(courseId);
        courseClass.setClassId(classId);
        courseClass.setCreatedAt(LocalDateTime.now());
        courseClass.setUpdatedAt(LocalDateTime.now());

        return courseClassMapper.insert(courseClass) > 0;
    }

    @Override
    @Transactional
    public boolean batchAssignCourseToClasses(String courseId, List<String> classIds, String assignerId) {
        if (classIds == null || classIds.isEmpty()) {
            return false;
        }

        int successCount = 0;
        for (String classId : classIds) {
            try {
                if (assignCourseToClass(courseId, classId, assignerId)) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他班级
                System.err.println("分配课程到班级失败: " + e.getMessage());
            }
        }
        return successCount > 0;
    }

    @Override
    @Transactional
    public boolean removeCourseFromClass(String courseId, String classId) {
        QueryWrapper<CourseClass> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).eq("class_id", classId);
        return courseClassMapper.delete(wrapper) > 0;
    }

    @Override
    public List<CourseClass> getCourseClasses(String courseId) {
        return courseClassMapper.selectByCourseId(courseId);
    }

    @Override
    public List<CourseClass> getClassCourses(String classId) {
        return courseClassMapper.selectByClassId(classId);
    }

    @Override
    public List<CourseSchedule> getStudentTimetableByWeek(String studentId, int weekNumber) {
        // 获取学生信息
        User student = userMapper.selectById(studentId);
        if (student == null || student.getUserRole() != 0) {
            throw new RuntimeException("学生不存在或不是学生角色");
        }

        // 获取周次对应的日期范围
        WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

        // 查询学生所在班级的课程安排
        return getSchedulesByStudentClass(student.getClassId(), weekRange.getStartDate(), weekRange.getEndDate());
    }

    @Override
    public List<CourseSchedule> getTeacherTimetableByWeek(String teacherId, int weekNumber) {
        WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

        // 查询教师课程安排
        QueryWrapper<CourseSchedule> wrapper = new QueryWrapper<>();
        wrapper.inSql("course_id",
                String.format("SELECT course_id FROM courses WHERE teacher_id = '%s'", teacherId));

        // 根据周次过滤
        wrapper.and(w -> w.and(w1 -> w1.eq("schedule_type", 0) // 每周重复课程
                        .apply("({0} BETWEEN start_week AND end_week)", weekNumber))
                .or(w2 -> w2.eq("schedule_type", 1) // 单次安排课程
                        .between("schedule_date", weekRange.getStartDate(), weekRange.getEndDate())));

        wrapper.orderByAsc("day_of_week").orderByAsc("start_time");
        return courseScheduleMapper.selectList(wrapper);
    }

    @Override
    public List<CourseSchedule> getClassTimetableByWeek(String classId, int weekNumber) {
        WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

        return courseScheduleMapper.selectSchedulesByClassAndWeek(
                classId,
                weekNumber,
                weekRange.getStartDate(),
                weekRange.getEndDate()
        );
    }

    @Override
    public List<CourseSchedule> getClassroomTimetableByWeek(String classroomId, int weekNumber) {
        WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

        // 使用带详细信息的查询（包含课程名称、教师姓名等）
        return courseScheduleMapper.selectClassroomSchedulesByWeek(
                classroomId,
                weekNumber,
                weekRange.getStartDate(),
                weekRange.getEndDate()
        );
    }

    @Override
    public int getCurrentWeek() {
        return WeekCalculator.getCurrentWeek();
    }

    @Override
    public int getWeekNumber(LocalDate date) {
        return WeekCalculator.getWeekNumber(date);
    }

    @Override
    public WeekCalculator.AcademicYearSemester getCurrentAcademicYearSemester() {
        return WeekCalculator.getCurrentAcademicYearSemester();
    }

    @Override
    public List<CourseSchedule> getSchedulesByWeek(String courseId, String classroomId,
                                                   String campusId, int weekNumber) {
        WeekCalculator.WeekDateRange weekRange = WeekCalculator.getDateRangeByWeek(weekNumber);

        QueryWrapper<CourseSchedule> wrapper = new QueryWrapper<>();

        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        if (classroomId != null) {
            wrapper.eq("classroom_id", classroomId);
        }
        if (campusId != null) {
            wrapper.eq("campus_id", campusId);
        }

        // 根据周次过滤
        wrapper.and(w -> w.and(w1 -> w1.eq("schedule_type", 0) // 每周重复
                        .apply("({0} BETWEEN start_week AND end_week)", weekNumber))
                .or(w2 -> w2.eq("schedule_type", 1) // 单次安排
                        .between("schedule_date", weekRange.getStartDate(), weekRange.getEndDate())));

        wrapper.orderByAsc("day_of_week").orderByAsc("start_time");
        return courseScheduleMapper.selectList(wrapper);
    }

    /**
     * 根据学生班级获取课程安排
     */
    private List<CourseSchedule> getSchedulesByStudentClass(String classId, LocalDate startDate, LocalDate endDate) {
        // 查询班级课程
        QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
        courseWrapper.eq("class_id", classId);
        List<Course> classCourses = courseMapper.selectList(courseWrapper);

        if (classCourses.isEmpty()) {
            return List.of();
        }

        List<String> courseIds = classCourses.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());

        // 查询课程安排
        QueryWrapper<CourseSchedule> scheduleWrapper = new QueryWrapper<>();
        scheduleWrapper.in("course_id", courseIds)
                .and(w -> w.and(w1 -> w1.eq("schedule_type", 0) // 每周重复
                                .apply("day_of_week IS NOT NULL"))
                        .or(w2 -> w2.eq("schedule_type", 1) // 单次安排
                                .between("schedule_date", startDate, endDate)));

        return courseScheduleMapper.selectList(scheduleWrapper);
    }
}