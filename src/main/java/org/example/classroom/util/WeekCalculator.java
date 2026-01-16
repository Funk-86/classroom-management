package org.example.classroom.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class WeekCalculator {

    /**
     * 获取当前学年学期
     */
    public static AcademicYearSemester getCurrentAcademicYearSemester() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        // 假设9月1日到次年2月底为秋季学期，3月1日到8月31日为春季学期
        if (month >= 9 || month <= 2) {
            // 秋季学期
            String academicYear = month >= 9 ?
                    year + "-" + (year + 1) :
                    (year - 1) + "-" + year;
            return new AcademicYearSemester(academicYear, 2); // 2表示秋季学期
        } else {
            // 春季学期
            String academicYear = year + "-" + (year + 1);
            return new AcademicYearSemester(academicYear, 1); // 1表示春季学期
        }
    }

    /**
     * 计算当前是第几周（基于学年开始日期）
     */
    public static int getCurrentWeek() {
        return getWeekNumber(LocalDate.now());
    }

    /**
     * 根据日期计算周次
     */
    public static int getWeekNumber(LocalDate date) {
        AcademicYearSemester current = getCurrentAcademicYearSemester();
        LocalDate startDate = getSemesterStartDate(current.getAcademicYear(), current.getSemester());

        // 计算相差的天数，然后转换为周数
        long daysBetween = ChronoUnit.DAYS.between(startDate, date);
        int weekNumber = (int) (daysBetween / 7) + 1;

        return Math.max(1, weekNumber); // 确保周数至少为1
    }

    /**
     * 根据周次获取日期范围
     */
    public static WeekDateRange getDateRangeByWeek(int weekNumber) {
        AcademicYearSemester current = getCurrentAcademicYearSemester();
        LocalDate startDate = getSemesterStartDate(current.getAcademicYear(), current.getSemester());

        LocalDate weekStart = startDate.plusWeeks(weekNumber - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        return new WeekDateRange(weekNumber, weekStart, weekEnd);
    }

    /**
     * 获取学期开始日期
     */
    private static LocalDate getSemesterStartDate(String academicYear, Integer semester) {
        // 解析学年，如"2025-2026"
        String[] years = academicYear.split("-");
        int startYear = Integer.parseInt(years[0]);

        if (semester == 1) {
            // 春季学期：3月1日
            return LocalDate.of(startYear, 3, 1);
        } else {
            // 秋季学期：9月1日
            return LocalDate.of(startYear, 9, 1);
        }
    }

    /**
     * 获取学期结束日期
     */
    private static LocalDate getSemesterEndDate(String academicYear, Integer semester) {
        // 解析学年，如"2025-2026"
        String[] years = academicYear.split("-");
        int startYear = Integer.parseInt(years[0]);

        if (semester == 1) {
            // 春季学期：8月31日
            return LocalDate.of(startYear, 8, 31);
        } else {
            // 秋季学期：次年2月28日（或29日，根据年份）
            int endYear = startYear + 1;
            // 检查是否为闰年
            if (endYear % 4 == 0 && (endYear % 100 != 0 || endYear % 400 == 0)) {
                return LocalDate.of(endYear, 2, 29);
            } else {
                return LocalDate.of(endYear, 2, 28);
            }
        }
    }

    /**
     * 检查指定日期是否在学期内
     */
    public static boolean isDateInSemester(LocalDate date) {
        AcademicYearSemester current = getCurrentAcademicYearSemester();
        LocalDate startDate = getSemesterStartDate(current.getAcademicYear(), current.getSemester());
        LocalDate endDate = getSemesterEndDate(current.getAcademicYear(), current.getSemester());
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 检查当前日期是否在学期内
     */
    public static boolean isCurrentDateInSemester() {
        return isDateInSemester(LocalDate.now());
    }

    /**
     * 学年学期信息类
     */
    public static class AcademicYearSemester {
        private String academicYear;
        private Integer semester;

        public AcademicYearSemester(String academicYear, Integer semester) {
            this.academicYear = academicYear;
            this.semester = semester;
        }

        public String getAcademicYear() { return academicYear; }
        public Integer getSemester() { return semester; }
    }

    /**
     * 周次日期范围类
     */
    public static class WeekDateRange {
        private int weekNumber;
        private LocalDate startDate;
        private LocalDate endDate;

        public WeekDateRange(int weekNumber, LocalDate startDate, LocalDate endDate) {
            this.weekNumber = weekNumber;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getWeekNumber() { return weekNumber; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
}

