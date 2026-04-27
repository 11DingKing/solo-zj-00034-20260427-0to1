package com.hospital.dto;

import com.hospital.entity.enums.TimeSlot;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDate scheduleDate;
    private TimeSlot timeSlot;
    private Integer maxAppointments;
    private Integer bookedCount;
    private Integer remainingSlots;
    private Boolean isActive;
    private Boolean isTemporaryAdjusted;
}
