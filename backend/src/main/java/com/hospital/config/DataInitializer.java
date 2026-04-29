package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.entity.enums.DoctorStatus;
import com.hospital.entity.enums.TimeSlot;
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
    private final ScheduleTemplateRepository templateRepository;
    private final SymptomDepartmentRepository symptomDepartmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initDepartments();
        initDoctors();
        initScheduleTemplates();
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

    private void initDoctors() {
        if (doctorRepository.count() > 0) {
            log.info("医生数据已存在");
            return;
        }

        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            log.warn("科室数据为空，跳过医生初始化");
            return;
        }

        String defaultPassword = "doctor123";

        for (Department dept : departments) {
            switch (dept.getName()) {
                case "内科" -> {
                    createDoctor("张医生", "主任医师", "心血管疾病、高血压、冠心病", 
                            "从医20年，擅长心血管疾病的诊断和治疗", dept.getId(), 
                            "doctor_zhang", defaultPassword);
                    createDoctor("李医生", "副主任医师", "呼吸系统疾病、哮喘、肺炎",
                            "从医15年，擅长呼吸系统疾病诊治", dept.getId(),
                            "doctor_li", defaultPassword);
                }
                case "外科" -> {
                    createDoctor("王医生", "主任医师", "普外科、胃肠手术、肝胆疾病",
                            "从医22年，擅长普外科手术治疗", dept.getId(),
                            "doctor_wang", defaultPassword);
                    createDoctor("刘医生", "副主任医师", "骨科、关节置换、脊柱疾病",
                            "从医12年，擅长骨科疾病诊治", dept.getId(),
                            "doctor_liu", defaultPassword);
                }
                case "儿科" -> {
                    createDoctor("陈医生", "主任医师", "儿童常见病、新生儿疾病、发育评估",
                            "从医18年，擅长儿科疾病诊治", dept.getId(),
                            "doctor_chen", defaultPassword);
                }
                case "妇产科" -> {
                    createDoctor("赵医生", "主任医师", "妇科肿瘤、高危妊娠、不孕不育",
                            "从医20年，擅长妇产科疾病诊治", dept.getId(),
                            "doctor_zhao", defaultPassword);
                    createDoctor("周医生", "副主任医师", "产前检查、产后恢复、妇科炎症",
                            "从医10年，擅长妇产科常见病", dept.getId(),
                            "doctor_zhou", defaultPassword);
                }
                case "眼科" -> {
                    createDoctor("吴医生", "主任医师", "白内障、青光眼、近视矫正",
                            "从医16年，擅长眼科手术治疗", dept.getId(),
                            "doctor_wu", defaultPassword);
                }
                case "耳鼻喉科" -> {
                    createDoctor("郑医生", "副主任医师", "鼻炎、鼻窦炎、中耳炎、听力障碍",
                            "从医14年，擅长耳鼻喉疾病诊治", dept.getId(),
                            "doctor_zheng", defaultPassword);
                }
                case "皮肤科" -> {
                    createDoctor("孙医生", "副主任医师", "湿疹、皮炎、痤疮、皮肤过敏",
                            "从医11年，擅长皮肤疾病诊治", dept.getId(),
                            "doctor_sun", defaultPassword);
                }
                case "口腔科" -> {
                    createDoctor("钱医生", "主任医师", "种植牙、正畸、牙周病、口腔修复",
                            "从医19年，擅长口腔疾病诊治", dept.getId(),
                            "doctor_qian", defaultPassword);
                }
            }
        }

        log.info("示例医生创建完成，密码统一为: doctor123");
    }

    private void createDoctor(String name, String title, String specialty, 
                              String introduction, Long deptId, 
                              String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.DOCTOR);
        user.setRealName(name);
        user.setIsActive(true);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setTitle(title);
        doctor.setSpecialty(specialty);
        doctor.setIntroduction(introduction);
        doctor.setDepartmentId(deptId);
        doctor.setUserId(user.getId());
        doctor.setStatus(DoctorStatus.AVAILABLE);
        doctorRepository.save(doctor);
    }

    private void initScheduleTemplates() {
        if (templateRepository.count() > 0) {
            log.info("排班模板已存在");
            return;
        }

        List<Doctor> doctors = doctorRepository.findAll();
        if (doctors.isEmpty()) {
            log.warn("医生数据为空，跳过排班模板初始化");
            return;
        }

        for (Doctor doctor : doctors) {
            for (int day = 1; day <= 5; day++) {
                createTemplate(doctor.getId(), day, TimeSlot.MORNING, 15);
                createTemplate(doctor.getId(), day, TimeSlot.AFTERNOON, 10);
            }
            createTemplate(doctor.getId(), 6, TimeSlot.MORNING, 10);
        }

        log.info("排班模板初始化完成（周一到周五全天，周六上午）");
    }

    private void createTemplate(Long doctorId, int dayOfWeek, TimeSlot timeSlot, int maxAppointments) {
        ScheduleTemplate template = new ScheduleTemplate();
        template.setDoctorId(doctorId);
        template.setDayOfWeek(dayOfWeek);
        template.setTimeSlot(timeSlot);
        template.setMaxAppointments(maxAppointments);
        template.setIsActive(true);
        templateRepository.save(template);
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
