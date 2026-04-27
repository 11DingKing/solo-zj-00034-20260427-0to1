package com.hospital.dto;

import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.entity.enums.TimeSlot;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long id;
    private String appointmentNo;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private LocalDate appointmentDate;
    private TimeSlot timeSlot;
    private Integer queueNumber;
    private String patientName;
    private String patientIdCard;
    private String patientPhone;
    private String symptoms;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}
