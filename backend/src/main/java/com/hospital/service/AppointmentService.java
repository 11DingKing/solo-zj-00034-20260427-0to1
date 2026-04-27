package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.entity.User;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.entity.enums.TimeSlot;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.ScheduleRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final ScheduleService scheduleService;
    private final AuthService authService;

    private static final String LOCK_PREFIX = "appointment:schedule:";
    private static final long LOCK_WAIT_TIME = 3;
    private static final long LOCK_LEASE_TIME = 10;

    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        User patient = authService.getCurrentUser();
        if (patient == null) {
            throw new RuntimeException("请先登录");
        }

        if (patient.isBanned()) {
            throw new RuntimeException("您因多次爽约，预约功能已被限制至: " + patient.getNoShowBanUntil());
        }

        TimeSlot timeSlot = TimeSlot.valueOf(request.getTimeSlot().toUpperCase());
        
        String lockKey = LOCK_PREFIX + request.getDoctorId() + ":" + 
                         request.getAppointmentDate() + ":" + timeSlot;
        
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("预约繁忙，请稍后重试");
            }

            try {
                Schedule schedule = scheduleService.getOrCreateSchedule(
                        request.getDoctorId(), 
                        request.getAppointmentDate(), 
                        timeSlot
                );

                if (schedule == null || !schedule.getIsActive()) {
                    throw new RuntimeException("该时段无可预约号源");
                }

                if (schedule.getRemainingSlots() <= 0) {
                    throw new RuntimeException("该时段号源已约满");
                }

                schedule.setBookedCount(schedule.getBookedCount() + 1);
                schedule = scheduleRepository.save(schedule);

                Appointment appointment = new Appointment();
                appointment.setAppointmentNo(generateAppointmentNo());
                appointment.setPatientId(patient.getId());
                appointment.setDoctorId(request.getDoctorId());
                appointment.setScheduleId(schedule.getId());
                appointment.setAppointmentDate(request.getAppointmentDate());
                appointment.setTimeSlot(timeSlot);
                appointment.setQueueNumber(schedule.getBookedCount());
                appointment.setPatientName(request.getPatientName());
                appointment.setPatientIdCard(request.getPatientIdCard());
                appointment.setPatientPhone(request.getPatientPhone());
                appointment.setSymptoms(request.getSymptoms());
                appointment.setStatus(AppointmentStatus.PENDING);

                return appointmentRepository.save(appointment);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("预约被中断", e);
        }
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String cancelReason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));

        User patient = authService.getCurrentUser();
        if (patient == null || !appointment.getPatientId().equals(patient.getId())) {
            throw new RuntimeException("无权取消该预约");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("该预约状态不可取消");
        }

        LocalTime cutoffTime = appointment.getTimeSlot() == TimeSlot.MORNING 
                ? LocalTime.of(8, 0) 
                : LocalTime.of(14, 0);
        LocalDateTime cutoffDateTime = LocalDateTime.of(appointment.getAppointmentDate(), cutoffTime);
        LocalDateTime twoHoursBefore = cutoffDateTime.minusHours(2);

        if (LocalDateTime.now().isAfter(twoHoursBefore)) {
            throw new RuntimeException("就诊前2小时内不可取消预约");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(cancelReason);
        appointmentRepository.save(appointment);

        Schedule schedule = scheduleRepository.findById(appointment.getScheduleId())
                .orElseThrow(() -> new RuntimeException("排班不存在"));
        schedule.setBookedCount(Math.max(0, schedule.getBookedCount() - 1));
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void markAsVisited(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        appointment.setStatus(AppointmentStatus.VISITED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void markAsNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);

        User patient = userRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new RuntimeException("患者不存在"));
        
        int newNoShowCount = patient.getNoShowCount() + 1;
        patient.setNoShowCount(newNoShowCount);

        if (newNoShowCount >= 3) {
            patient.setNoShowBanUntil(LocalDateTime.now().plusDays(30));
            patient.setNoShowCount(0);
        }

        userRepository.save(patient);
    }

    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdWithDoctorAndDepartment(patientId);
    }

    public List<Appointment> getDoctorAppointments(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndDateWithPatient(doctorId, date);
    }

    public List<Appointment> getDoctorUpcomingAppointments(Long doctorId) {
        return appointmentRepository.findUpcomingAppointmentsByDoctor(doctorId, LocalDate.now());
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
    }

    private String generateAppointmentNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "APT" + date + uuid;
    }
}
