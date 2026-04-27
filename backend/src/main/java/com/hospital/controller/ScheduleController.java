package com.hospital.controller;

import com.hospital.dto.ScheduleDTO;
import com.hospital.dto.ScheduleTemplateDTO;
import com.hospital.entity.Schedule;
import com.hospital.entity.ScheduleTemplate;
import com.hospital.entity.enums.TimeSlot;
import com.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/public/doctor/{doctorId}")
    public ResponseEntity<List<ScheduleDTO>> getDoctorSchedules(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Schedule> schedules = scheduleService.getDoctorSchedules(doctorId, startDate, endDate);
        List<ScheduleDTO> dtos = schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/templates/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScheduleTemplate>> getTemplates(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getTemplatesByDoctor(doctorId));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTemplate(@RequestBody ScheduleTemplate template) {
        try {
            ScheduleTemplate created = scheduleService.createTemplate(template);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @RequestBody ScheduleTemplate template) {
        try {
            ScheduleTemplate updated = scheduleService.updateTemplate(id, template);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            scheduleService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/copy-week")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> copyWeekSchedule(@RequestBody Map<String, Object> request) {
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            LocalDate fromWeekStart = LocalDate.parse(request.get("fromWeekStart").toString());
            LocalDate toWeekStart = LocalDate.parse(request.get("toWeekStart").toString());
            
            scheduleService.copyWeekSchedule(doctorId, fromWeekStart, toWeekStart);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adjustSchedule(@RequestBody Map<String, Object> request) {
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            LocalDate date = LocalDate.parse(request.get("date").toString());
            TimeSlot timeSlot = TimeSlot.valueOf(request.get("timeSlot").toString().toUpperCase());
            Boolean isActive = request.get("isActive") != null ? (Boolean) request.get("isActive") : null;
            Integer maxAppointments = request.get("maxAppointments") != null ? 
                Integer.valueOf(request.get("maxAppointments").toString()) : null;
            
            scheduleService.adjustScheduleTemporarily(doctorId, date, timeSlot, isActive, maxAppointments);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctorId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setTimeSlot(schedule.getTimeSlot());
        dto.setMaxAppointments(schedule.getMaxAppointments());
        dto.setBookedCount(schedule.getBookedCount());
        dto.setRemainingSlots(schedule.getRemainingSlots());
        dto.setIsActive(schedule.getIsActive());
        dto.setIsTemporaryAdjusted(schedule.getIsTemporaryAdjusted());
        return dto;
    }
}
