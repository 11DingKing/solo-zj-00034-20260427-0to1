package com.hospital.service;

import com.hospital.dto.DashboardStats;
import com.hospital.dto.DepartmentAppointmentStats;
import com.hospital.dto.DoctorWorkloadStats;
import com.hospital.dto.DailyTrend;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        stats.setTodayAppointments(appointmentRepository.countByDate(today));
        stats.setLast30DaysAppointments(appointmentRepository.countByDateRange(thirtyDaysAgo, today));
        
        long totalLast30Days = stats.getLast30DaysAppointments();
        long noShowLast30Days = appointmentRepository.countNoShowByDateRange(thirtyDaysAgo, today);
        stats.setNoShowRate(totalLast30Days > 0 ? (double) noShowLast30Days / totalLast30Days * 100 : 0);

        stats.setDepartmentStats(getDepartmentStats());
        stats.setDoctorStats(getDoctorStats());
        stats.setDailyTrends(getDailyTrends(thirtyDaysAgo, today));
        stats.setCalendarView(getCalendarView());

        return stats;
    }

    private List<DepartmentAppointmentStats> getDepartmentStats() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentAppointmentStats> stats = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        for (Department dept : departments) {
            DepartmentAppointmentStats stat = new DepartmentAppointmentStats();
            stat.setDepartmentId(dept.getId());
            stat.setDepartmentName(dept.getName());
            stat.setCount(0);
            stats.add(stat);
        }

        return stats;
    }

    private List<DoctorWorkloadStats> getDoctorStats() {
        List<Doctor> doctors = doctorRepository.findAllWithDepartment();
        List<DoctorWorkloadStats> stats = new ArrayList<>();

        for (Doctor doctor : doctors) {
            DoctorWorkloadStats stat = new DoctorWorkloadStats();
            stat.setDoctorId(doctor.getId());
            stat.setDoctorName(doctor.getName());
            stat.setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : "");
            stat.setCount(0);
            stats.add(stat);
        }

        return stats.stream()
                .sorted(Comparator.comparingLong(DoctorWorkloadStats::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DailyTrend> getDailyTrends(LocalDate startDate, LocalDate endDate) {
        List<DailyTrend> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DailyTrend trend = new DailyTrend();
            trend.setDate(current.format(formatter));
            trend.setCount(0);
            trends.add(trend);
            current = current.plusDays(1);
        }

        return trends;
    }

    private Map<String, Object> getCalendarView() {
        Map<String, Object> calendarView = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        
        List<String> weekDays = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
        List<Department> departments = departmentRepository.findByIsActiveTrue();
        
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (Department dept : departments) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept.getName());
            row.put("departmentId", dept.getId());
            
            List<Map<String, Object>> daysData = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                Map<String, Object> dayData = new LinkedHashMap<>();
                dayData.put("date", date.toString());
                dayData.put("weekDay", weekDays.get(i));
                dayData.put("total", 0);
                dayData.put("booked", 0);
                dayData.put("rate", 0.0);
                dayData.put("intensity", "none");
                daysData.add(dayData);
            }
            row.put("days", daysData);
            rows.add(row);
        }
        
        calendarView.put("rows", rows);
        calendarView.put("weekStart", startOfWeek.toString());
        
        return calendarView;
    }

    public Map<String, Object> getDetailedCalendarView(LocalDate weekStart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        if (weekStart == null) {
            weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        }
        
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Schedule> schedules = scheduleRepository.findByDateRange(weekStart, weekEnd);
        
        Map<Long, Map<LocalDate, List<Schedule>>> deptDateSchedules = new HashMap<>();
        
        for (Schedule schedule : schedules) {
            Doctor doctor = schedule.getDoctor();
            if (doctor != null) {
                Long deptId = doctor.getDepartmentId();
                deptDateSchedules.computeIfAbsent(deptId, k -> new HashMap<>())
                        .computeIfAbsent(schedule.getScheduleDate(), k -> new ArrayList<>())
                        .add(schedule);
            }
        }

        List<String> weekDays = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
        List<Department> departments = departmentRepository.findByIsActiveTrue();
        
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (Department dept : departments) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept.getName());
            row.put("departmentId", dept.getId());
            
            Map<LocalDate, List<Schedule>> dateSchedules = deptDateSchedules.getOrDefault(dept.getId(), new HashMap<>());
            
            List<Map<String, Object>> daysData = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStart.plusDays(i);
                List<Schedule> daySchedules = dateSchedules.getOrDefault(date, new ArrayList<>());
                
                int total = daySchedules.stream().mapToInt(Schedule::getMaxAppointments).sum();
                int booked = daySchedules.stream().mapToInt(Schedule::getBookedCount).sum();
                double rate = total > 0 ? (double) booked / total : 0;
                
                String intensity;
                if (rate >= 0.8) intensity = "high";
                else if (rate >= 0.5) intensity = "medium";
                else if (rate > 0) intensity = "low";
                else intensity = "none";
                
                Map<String, Object> dayData = new LinkedHashMap<>();
                dayData.put("date", date.toString());
                dayData.put("weekDay", weekDays.get(i));
                dayData.put("total", total);
                dayData.put("booked", booked);
                dayData.put("rate", rate);
                dayData.put("intensity", intensity);
                daysData.add(dayData);
            }
            row.put("days", daysData);
            rows.add(row);
        }
        
        result.put("rows", rows);
        result.put("weekStart", weekStart.toString());
        result.put("weekEnd", weekEnd.toString());
        
        return result;
    }
}
