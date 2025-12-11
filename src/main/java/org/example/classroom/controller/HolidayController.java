package org.example.classroom.controller;

import org.example.classroom.dto.R;
import org.example.classroom.entity.Holiday;
import org.example.classroom.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping
    public R list() {
        List<Holiday> list = holidayService.list();
        return R.ok().put("data", list);
    }

    @PostMapping
    public R create(@RequestBody Holiday holiday) {
        if (holiday.getName() == null || holiday.getStartDate() == null || holiday.getEndDate() == null) {
            return R.error("名称与起止日期必填");
        }
        holiday.setStatus(holiday.getStatus() == null ? 1 : holiday.getStatus());
        boolean ok = holidayService.save(holiday);
        return ok ? R.ok("创建成功") : R.error("创建失败");
    }

    @PutMapping("/{id}")
    public R update(@PathVariable String id, @RequestBody Holiday holiday) {
        holiday.setHolidayId(id);
        boolean ok = holidayService.updateById(holiday);
        return ok ? R.ok("更新成功") : R.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable String id) {
        boolean ok = holidayService.removeById(id);
        return ok ? R.ok("删除成功") : R.error("删除失败");
    }
}

