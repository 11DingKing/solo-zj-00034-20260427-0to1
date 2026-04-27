package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.entity.ScheduleTemplate;
import com.hospital.entity.enums.TimeSlot;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.ScheduleRepository;
import com.hospital.repository.ScheduleTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleTemplateRepository templateRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public List<ScheduleTemplate> getTemplatesByDoctor(Long doctorId) {
        return templateRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    @Transactional
    public ScheduleTemplate createTemplate(ScheduleTemplate template) {
        Optional<ScheduleTemplate> existing = templateRepository.findByDoctorIdAndDayOfWeekAndTimeSlot(
                template.getDoctorId(), template.getDayOfWeek(), template.getTimeSlot()
        );
        if (existing.isPresent()) {
            throw new RuntimeException("该时段的排班模板已存在");
        }
        return templateRepository.save(template);
    }

    @Transactional
    public ScheduleTemplate updateTemplate(Long id, ScheduleTemplate templateDetails) {
        ScheduleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("排班模板不存在"));
        template.setDayOfWeek(templateDetails.getDayOfWeek());
        template.setTimeSlot(templateDetails.getTimeSlot());
        template.setMaxAppointments(templateDetails.getMaxAppointments());
        template.setIsActive(templateDetails.getIsActive());
        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    @Transactional
    public void copyWeekSchedule(Long doctorId, LocalDate fromWeekStart, LocalDate toWeekStart) {
        LocalDate fromWeekEnd = fromWeekStart.plusDays(6);
        List<Schedule> sourceSchedules = scheduleRepository.findByDoctorIdAndScheduleDateBetween(
                doctorId, fromWeekStart, fromWeekEnd
        );

        for (Schedule source : sourceSchedules) {
            long daysDiff = ChronoUnit.DAYS.between(fromWeekStart, toWeekStart);
            LocalDate newDate = source.getScheduleDate().plusDays(daysDiff);

            Optional<Schedule> existing = scheduleRepository.findByDoctorIdAndScheduleDateAndTimeSlot(
                    doctorId, newDate, source.getTimeSlot()
            );

            if (existing.isEmpty()) {
                Schedule newSchedule = new Schedule();
                newSchedule.setDoctorId(doctorId);
                newSchedule.setScheduleDate(newDate);
                newSchedule.setTimeSlot(source.getTimeSlot());
                newSchedule.setMaxAppointments(source.getMaxAppointments());
                newSchedule.setBookedCount(0);
                newSchedule.setIsActive(source.getIsActive());
                newSchedule.setIsTemporaryAdjusted(false);
                scheduleRepository.save(newSchedule);
            } else {
                Schedule schedule = existing.get();
                if (!schedule.getIsTemporaryAdjusted()) {
                    schedule.setMaxAppointments(source.getMaxAppointments());
                    schedule.setIsActive(source.getIsActive());
                    scheduleRepository.save(schedule);
                }
            }
        }
    }

    @Transactional
    public void generateSchedulesFromTemplates(Long doctorId, LocalDate startDate, LocalDate endDate) {
        List<ScheduleTemplate> templates = templateRepository.findByDoctorIdAndIsActiveTrue(doctorId);
        if (templates.isEmpty()) {
            return;
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = convertToJavaDayOfWeek(current.getDayOfWeek());
            
            for (ScheduleTemplate template : templates) {
                if (template.getDayOfWeek().equals(dayOfWeek)) {
                    Optional<Schedule> existing = scheduleRepository.findByDoctorIdAndScheduleDateAndTimeSlot(
                            doctorId, current, template.getTimeSlot()
                    );

                    if (existing.isEmpty()) {
                        Schedule schedule = new Schedule();
                        schedule.setDoctorId(doctorId);
                        schedule.setScheduleDate(current);
                        schedule.setTimeSlot(template.getTimeSlot());
                        schedule.setMaxAppointments(template.getMaxAppointments());
                        schedule.setBookedCount(0);
                        schedule.setIsActive(true);
                        schedule.setIsTemporaryAdjusted(false);
                        scheduleRepository.save(schedule);
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    @Transactional
    public void adjustScheduleTemporarily(Long doctorId, LocalDate date, TimeSlot timeSlot, 
                                            Boolean isActive, Integer maxAppointments) {
        Optional<Schedule> existing = scheduleRepository.findByDoctorIdAndScheduleDateAndTimeSlot(
                doctorId, date, timeSlot
        );

        if (existing.isPresent()) {
            Schedule schedule = existing.get();
            if (isActive != null) {
                schedule.setIsActive(isActive);
            }
            if (maxAppointments != null && maxAppointments >= schedule.getBookedCount()) {
                schedule.setMaxAppointments(maxAppointments);
            }
            schedule.setIsTemporaryAdjusted(true);
            scheduleRepository.save(schedule);
        } else if (isActive != null && isActive) {
            Schedule schedule = new Schedule();
            schedule.setDoctorId(doctorId);
            schedule.setScheduleDate(date);
            schedule.setTimeSlot(timeSlot);
            schedule.setMaxAppointments(maxAppointments != null ? maxAppointments : 10);
            schedule.setBookedCount(0);
            schedule.setIsActive(true);
            schedule.setIsTemporaryAdjusted(true);
            scheduleRepository.save(schedule);
        }
    }

    public List<Schedule> getDoctorSchedules(Long doctorId, LocalDate startDate, LocalDate endDate) {
        generateSchedulesFromTemplates(doctorId, startDate, endDate);
        return scheduleRepository.findActiveSchedulesByDoctorAndDateRange(doctorId, startDate, endDate);
    }

    public List<Schedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByDateRange(startDate, endDate);
    }

    public Schedule getOrCreateSchedule(Long doctorId, LocalDate date, TimeSlot timeSlot) {
        Optional<Schedule> existing = scheduleRepository.findByDoctorIdAndScheduleDateAndTimeSlot(
                doctorId, date, timeSlot
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        int dayOfWeek = convertToJavaDayOfWeek(date.getDayOfWeek());
        Optional<ScheduleTemplate> template = templateRepository.findByDoctorIdAndDayOfWeekAndTimeSlot(
                doctorId, dayOfWeek, timeSlot
        );

        if (template.isPresent() && template.get().getIsActive()) {
            Schedule schedule = new Schedule();
            schedule.setDoctorId(doctorId);
            schedule.setScheduleDate(date);
            schedule.setTimeSlot(timeSlot);
            schedule.setMaxAppointments(template.get().getMaxAppointments());
            schedule.setBookedCount(0);
            schedule.setIsActive(true);
            schedule.setIsTemporaryAdjusted(false);
            return scheduleRepository.save(schedule);
        }

        return null;
    }

    private int convertToJavaDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
            case SUNDAY -> 7;
        };
    }
}
