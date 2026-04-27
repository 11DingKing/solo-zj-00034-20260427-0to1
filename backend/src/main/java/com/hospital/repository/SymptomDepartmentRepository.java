package com.hospital.repository;

import com.hospital.entity.SymptomDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomDepartmentRepository extends JpaRepository<SymptomDepartment, Long> {
    @Query("SELECT sd FROM SymptomDepartment sd JOIN FETCH sd.department WHERE LOWER(sd.symptomKeyword) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SymptomDepartment> findByKeywordContaining(@Param("keyword") String keyword);
    
    List<SymptomDepartment> findByDepartmentId(Long departmentId);
}
