package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentRequest {
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    @NotBlank(message = "时段不能为空")
    private String timeSlot;

    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    private String patientIdCard;

    private String patientPhone;

    private String symptoms;
}
