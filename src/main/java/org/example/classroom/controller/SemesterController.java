package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.Semester;
import org.example.classroom.service.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;

    @GetMapping
    public R list() {
        List<Semester> list = semesterService.list();
        return R.ok().put("data", list);
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable String id) {
        Semester semester = semesterService.getById(id);
        if (semester != null) {
            return R.ok().put("data", semester);
        } else {
            return R.error("学期不存在");
        }
    }

    @PostMapping
    public R create(@RequestBody Semester semester) {
        if (semester.getName() == null || semester.getAcademicYear() == null ||
                semester.getStartDate() == null || semester.getEndDate() == null) {
            return R.error("名称/学年/起止日期必填");
        }
        semester.setWeeks(semester.getWeeks() == null ? 20 : semester.getWeeks());
        boolean ok = semesterService.save(semester);
        return ok ? R.ok("创建成功") : R.error("创建失败");
    }

    @PutMapping("/{id}")
    public R update(@PathVariable String id, @RequestBody Semester semester) {
        semester.setSemesterId(id);
        boolean ok = semesterService.updateById(semester);
        return ok ? R.ok("更新成功") : R.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable String id) {
        boolean ok = semesterService.removeById(id);
        return ok ? R.ok("删除成功") : R.error("删除失败");
    }
}

