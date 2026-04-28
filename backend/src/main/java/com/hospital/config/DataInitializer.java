package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.entity.enums.DoctorStatus;
import com.hospital.entity.enums.UserRole;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final SymptomDepartmentRepository symptomDepartmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initDepartments();
        initSymptomMapping();
    }

    private void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setRealName("系统管理员");
            admin.setIsActive(true);
            userRepository.save(admin);
            log.info("管理员账号创建成功: admin / admin123");
        } else {
            log.info("管理员账号已存在");
        }
    }

    private void initDepartments() {
        if (departmentRepository.count() == 0) {
            List<Department> departments = Arrays.asList(
                createDepartment("内科", "内科诊疗中心，擅长心血管、呼吸、消化等内科疾病", "门诊楼3层"),
                createDepartment("外科", "外科诊疗中心，擅长普外科、骨科等手术治疗", "门诊楼2层"),
                createDepartment("儿科", "儿科诊疗中心，擅长儿童常见疾病诊治", "门诊楼1层"),
                createDepartment("妇产科", "妇产科诊疗中心，擅长妇科、产科疾病", "门诊楼4层"),
                createDepartment("眼科", "眼科诊疗中心，擅长眼科疾病诊治", "门诊楼5层"),
                createDepartment("耳鼻喉科", "耳鼻喉科诊疗中心，擅长耳鼻喉疾病", "门诊楼5层"),
                createDepartment("皮肤科", "皮肤科诊疗中心，擅长皮肤疾病诊治", "门诊楼1层"),
                createDepartment("口腔科", "口腔科诊疗中心，擅长口腔疾病诊治", "门诊楼2层")
            );
            departmentRepository.saveAll(departments);
            log.info("科室数据初始化完成，共 {} 个科室", departments.size());
        } else {
            log.info("科室数据已存在");
        }
    }

    private Department createDepartment(String name, String description, String floorLocation) {
        Department dept = new Department();
        dept.setName(name);
        dept.setDescription(description);
        dept.setFloorLocation(floorLocation);
        dept.setIsActive(true);
        return dept;
    }

    private void initSymptomMapping() {
        if (symptomDepartmentRepository.count() == 0) {
            List<Department> departments = departmentRepository.findAll();
            if (departments.isEmpty()) {
                log.warn("科室数据为空，跳过症状映射初始化");
                return;
            }

            for (Department dept : departments) {
                Long deptId = dept.getId();
                switch (dept.getName()) {
                    case "内科" -> addSymptomMapping(deptId, Arrays.asList("咳嗽", "发烧", "感冒", "头痛", "胸闷", "腹痛", "腹泻", "恶心", "呕吐"), 1);
                    case "外科" -> addSymptomMapping(deptId, Arrays.asList("骨折", "外伤", "疼痛", "肿块"), 1);
                    case "儿科" -> addSymptomMapping(deptId, Arrays.asList("发热", "哭闹", "食欲不振", "发育"), 1);
                    case "妇产科" -> addSymptomMapping(deptId, Arrays.asList("月经", "怀孕", "白带", "腹痛"), 1);
                    case "眼科" -> addSymptomMapping(deptId, Arrays.asList("视力", "眼睛", "失明", "红肿"), 1);
                    case "耳鼻喉科" -> addSymptomMapping(deptId, Arrays.asList("耳朵", "鼻子", "喉咙", "听力"), 1);
                    case "皮肤科" -> addSymptomMapping(deptId, Arrays.asList("皮肤", "瘙痒", "皮疹", "过敏"), 1);
                    case "口腔科" -> addSymptomMapping(deptId, Arrays.asList("牙齿", "口腔", "牙龈", "牙痛"), 1);
                }
            }
            log.info("症状-科室映射初始化完成");
        } else {
            log.info("症状-科室映射已存在");
        }
    }

    private void addSymptomMapping(Long deptId, List<String> symptoms, int priority) {
        for (String symptom : symptoms) {
            SymptomDepartment mapping = new SymptomDepartment();
            mapping.setSymptomKeyword(symptom);
            mapping.setDepartmentId(deptId);
            mapping.setPriority(priority);
            symptomDepartmentRepository.save(mapping);
        }
    }
}
