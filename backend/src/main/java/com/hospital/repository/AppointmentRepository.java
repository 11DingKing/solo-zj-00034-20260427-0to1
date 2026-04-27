package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(Long doctorId, LocalDate date, AppointmentStatus status);
    Optional<Appointment> findByAppointmentNo(String appointmentNo);
    
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId ORDER BY a.createdAt DESC")
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") Long patientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate >= :date AND a.status = 'PENDING' ORDER BY a.appointmentDate ASC, a.queueNumber ASC")
    List<Appointment> findUpcomingAppointmentsByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' AND a.appointmentDate BETWEEN :startDate AND :endDate")
    long countNoShowByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a JOIN FETCH a.doctor d JOIN FETCH d.department WHERE a.patientId = :patientId ORDER BY a.appointmentDate DESC")
    List<Appointment> findByPatientIdWithDoctorAndDepartment(@Param("patientId") Long patientId);
    
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient WHERE a.doctorId = :doctorId AND a.appointmentDate = :date ORDER BY a.queueNumber ASC")
    List<Appointment> findByDoctorIdAndDateWithPatient(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
}
