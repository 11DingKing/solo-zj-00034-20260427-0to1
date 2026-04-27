package com.hospital.service;

import com.hospital.dto.RegisterRequest;
import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.entity.enums.DoctorStatus;
import com.hospital.entity.enums.UserRole;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllWithDepartment();
    }

    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndStatus(departmentId, DoctorStatus.AVAILABLE);
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
    }

    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor, String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(doctor.getName());
        user.setRole(UserRole.DOCTOR);
        user.setIsActive(true);
        user = userRepository.save(user);

        doctor.setUserId(user.getId());
        doctor.setStatus(DoctorStatus.AVAILABLE);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(doctorDetails.getName());
        doctor.setTitle(doctorDetails.getTitle());
        doctor.setSpecialty(doctorDetails.getSpecialty());
        doctor.setIntroduction(doctorDetails.getIntroduction());
        doctor.setAvatar(doctorDetails.getAvatar());
        doctor.setDepartmentId(doctorDetails.getDepartmentId());
        doctor.setStatus(doctorDetails.getStatus());
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
        userRepository.deleteById(doctor.getUserId());
    }
}
