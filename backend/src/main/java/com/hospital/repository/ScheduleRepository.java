package com.hospital.repository;

import com.hospital.entity.Schedule;
import com.hospital.entity.enums.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctorId(Long doctorId);
    List<Schedule> findByDoctorIdAndScheduleDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByScheduleDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<Schedule> findByDoctorIdAndScheduleDateAndTimeSlot(Long doctorId, LocalDate scheduleDate, TimeSlot timeSlot);
    
    @Query("SELECT s FROM Schedule s WHERE s.doctorId = :doctorId AND s.scheduleDate BETWEEN :startDate AND :endDate AND s.isActive = true")
    List<Schedule> findActiveSchedulesByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT s FROM Schedule s WHERE s.scheduleDate BETWEEN :startDate AND :endDate")
    List<Schedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Schedule s JOIN FETCH s.doctor d JOIN FETCH d.department WHERE s.scheduleDate = :date")
    List<Schedule> findByDateWithDoctorAndDepartment(@Param("date") LocalDate date);
}
