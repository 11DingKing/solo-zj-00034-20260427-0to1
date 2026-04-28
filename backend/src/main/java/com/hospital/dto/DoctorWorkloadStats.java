package com.hospital.dto;

import lombok.Data;

@Data
public class DoctorWorkloadStats {
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private long count;
}
