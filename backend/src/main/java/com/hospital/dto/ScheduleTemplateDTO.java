package com.hospital.dto;

import com.hospital.entity.enums.TimeSlot;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleTemplateDTO {
    private Long id;
    private Long doctorId;
    private Integer dayOfWeek;
    private TimeSlot timeSlot;
    private Integer maxAppointments;
    private Boolean isActive;
}
