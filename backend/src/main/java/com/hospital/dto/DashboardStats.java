package com.hospital.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardStats {
    private long todayAppointments;
    private long last30DaysAppointments;
    private double noShowRate;
    
    private List<DepartmentAppointmentStats> departmentStats;
    private List<DoctorWorkloadStats> doctorStats;
    private List<DailyTrend> dailyTrends;
    private Map<String, Object> calendarView;
}

@Data
class DepartmentAppointmentStats {
    private Long departmentId;
    private String departmentName;
    private long count;
}

@Data
class DoctorWorkloadStats {
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private long count;
}

@Data
class DailyTrend {
    private String date;
    private long count;
}
