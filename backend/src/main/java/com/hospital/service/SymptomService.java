package com.hospital.service;

import com.hospital.entity.Department;
import com.hospital.entity.SymptomDepartment;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.SymptomDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomDepartmentRepository symptomDepartmentRepository;
    private final DepartmentRepository departmentRepository;

    public List<Department> recommendDepartments(String symptoms) {
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return departmentRepository.findByIsActiveTrue();
        }

        String[] keywords = symptoms.trim().split("[\\s,，。、；;]+");
        
        Map<Long, Integer> deptScores = new HashMap<>();
        Map<Long, Department> deptMap = new HashMap<>();

        for (String keyword : keywords) {
            if (keyword.length() < 1) continue;
            
            List<SymptomDepartment> matches = symptomDepartmentRepository.findByKeywordContaining(keyword);
            
            for (SymptomDepartment match : matches) {
                Long deptId = match.getDepartmentId();
                int score = match.getPriority() * 10;
                
                if (match.getSymptomKeyword().equals(keyword)) {
                    score += 50;
                }
                
                deptScores.merge(deptId, score, Integer::sum);
                
                if (match.getDepartment() != null) {
                    deptMap.put(deptId, match.getDepartment());
                }
            }
        }

        if (deptScores.isEmpty()) {
            return departmentRepository.findByIsActiveTrue();
        }

        return deptScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(entry -> deptMap.computeIfAbsent(entry.getKey(), 
                    k -> departmentRepository.findById(k).orElse(null)))
                .filter(Objects::nonNull)
                .filter(Department::getIsActive)
                .collect(Collectors.toList());
    }

    public List<SymptomDepartment> getAllMappings() {
        return symptomDepartmentRepository.findAll();
    }

    public SymptomDepartment addMapping(SymptomDepartment mapping) {
        return symptomDepartmentRepository.save(mapping);
    }

    public void deleteMapping(Long id) {
        symptomDepartmentRepository.deleteById(id);
    }
}
