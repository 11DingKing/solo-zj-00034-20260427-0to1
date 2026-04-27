package com.hospital.controller;

import com.hospital.entity.Department;
import com.hospital.entity.SymptomDepartment;
import com.hospital.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping("/recommend")
    public ResponseEntity<List<Department>> recommendDepartments(@RequestBody Map<String, String> request) {
        String symptoms = request.get("symptoms");
        List<Department> departments = symptomService.recommendDepartments(symptoms);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/mappings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SymptomDepartment>> getAllMappings() {
        return ResponseEntity.ok(symptomService.getAllMappings());
    }

    @PostMapping("/mappings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SymptomDepartment> addMapping(@RequestBody SymptomDepartment mapping) {
        return ResponseEntity.ok(symptomService.addMapping(mapping));
    }

    @DeleteMapping("/mappings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMapping(@PathVariable Long id) {
        symptomService.deleteMapping(id);
        return ResponseEntity.ok().build();
    }
}
