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
