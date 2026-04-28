package com.hospital.dto;

import lombok.Data;

@Data
public class DepartmentAppointmentStats {
    private Long departmentId;
    private String departmentName;
    private long count;
}
