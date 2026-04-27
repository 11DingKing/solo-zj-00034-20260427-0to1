package com.hospital.repository;

import com.hospital.entity.Doctor;
import com.hospital.entity.enums.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByStatus(DoctorStatus status);
    List<Doctor> findByDepartmentIdAndStatus(Long departmentId, DoctorStatus status);
    Optional<Doctor> findByUserId(Long userId);
    
    @Query("SELECT d FROM Doctor d JOIN FETCH d.department WHERE d.id = :id")
    Optional<Doctor> findByIdWithDepartment(@Param("id") Long id);
    
    @Query("SELECT d FROM Doctor d JOIN FETCH d.department")
    List<Doctor> findAllWithDepartment();
}
