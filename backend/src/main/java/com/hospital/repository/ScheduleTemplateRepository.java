package com.hospital.repository;

import com.hospital.entity.ScheduleTemplate;
import com.hospital.entity.enums.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Long> {
    List<ScheduleTemplate> findByDoctorId(Long doctorId);
    List<ScheduleTemplate> findByDoctorIdAndIsActiveTrue(Long doctorId);
    Optional<ScheduleTemplate> findByDoctorIdAndDayOfWeekAndTimeSlot(Long doctorId, Integer dayOfWeek, TimeSlot timeSlot);
    void deleteByDoctorId(Long doctorId);
}
