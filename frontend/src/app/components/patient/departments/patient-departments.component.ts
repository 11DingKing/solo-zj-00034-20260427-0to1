import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import {
  DepartmentService,
  Department,
} from "../../../services/department.service";
import { DoctorService, Doctor } from "../../../services/doctor.service";
import { ScheduleService, Schedule } from "../../../services/schedule.service";
import {
  AppointmentService,
  AppointmentRequest,
} from "../../../services/appointment.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

@Component({
  selector: "app-patient-departments",
  templateUrl: "./patient-departments.component.html",
  styleUrls: ["./patient-departments.component.css"],
})
export class PatientDepartmentsComponent implements OnInit {
  departments: Department[] = [];
  doctors: Doctor[] = [];
  selectedDepartment: Department | null = null;
  selectedDoctor: Doctor | null = null;
  doctorSchedules: Schedule[] = [];
  showBookingModal = false;
  selectedSchedule: Schedule | null = null;
  loading = false;
  submitting = false;
  error = "";
  success = "";

  bookingForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private departmentService: DepartmentService,
    private doctorService: DoctorService,
    private scheduleService: ScheduleService,
    private appointmentService: AppointmentService,
    private fb: FormBuilder,
  ) {
    this.bookingForm = this.fb.group({
      patientName: ["", Validators.required],
      patientIdCard: [""],
      patientPhone: ["", Validators.pattern(/^1[3-9]\d{9}$/)],
      symptoms: [""],
    });
  }

  ngOnInit(): void {
    this.loadDepartments();

    const deptId = this.route.snapshot.paramMap.get("deptId");
    if (deptId) {
      this.loadDoctors(parseInt(deptId));
    }
  }

  loadDepartments(): void {
    this.departmentService.getPublicDepartments().subscribe({
      next: (data: Department[]) => {
        this.departments = data;
      },
    });
  }

  loadDoctors(deptId: number): void {
    this.loading = true;
    this.doctorService.getByDepartment(deptId).subscribe({
      next: (data: Doctor[]) => {
        this.doctors = data;
        this.selectedDepartment =
          this.departments.find((d) => d.id === deptId) || null;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  selectDepartment(dept: Department): void {
    this.router.navigate(["/patient/departments", dept.id]);
    this.loadDoctors(dept.id);
  }

  selectDoctor(doctor: Doctor): void {
    this.selectedDoctor = doctor;
    this.loadDoctorSchedules(doctor.id);
  }

  loadDoctorSchedules(doctorId: number): void {
    const today = new Date();
    const startDate = this.formatDate(today);
    const endDate = this.formatDate(
      new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000),
    );

    this.scheduleService
      .getDoctorSchedules(doctorId, startDate, endDate)
      .subscribe({
        next: (data: Schedule[]) => {
          this.doctorSchedules = data.filter(
            (s: Schedule) => s.isActive && s.remainingSlots > 0,
          );
        },
      });
  }

  openBookingModal(schedule: Schedule): void {
    this.selectedSchedule = schedule;
    this.showBookingModal = true;
    this.error = "";
    this.success = "";
    this.bookingForm.reset();
  }

  closeBookingModal(): void {
    this.showBookingModal = false;
    this.selectedSchedule = null;
  }

  submitBooking(): void {
    if (
      this.bookingForm.invalid ||
      !this.selectedSchedule ||
      !this.selectedDoctor
    ) {
      return;
    }

    this.submitting = true;
    this.error = "";

    const request: AppointmentRequest = {
      doctorId: this.selectedDoctor.id,
      appointmentDate: this.selectedSchedule.scheduleDate,
      timeSlot: this.selectedSchedule.timeSlot,
      patientName: this.bookingForm.value.patientName,
      patientIdCard: this.bookingForm.value.patientIdCard || undefined,
      patientPhone: this.bookingForm.value.patientPhone || undefined,
      symptoms: this.bookingForm.value.symptoms || undefined,
    };

    this.appointmentService.create(request).subscribe({
      next: () => {
        this.success = "预约成功！";
        this.submitting = false;
        setTimeout(() => {
          this.closeBookingModal();
          if (this.selectedDoctor) {
            this.loadDoctorSchedules(this.selectedDoctor.id);
          }
        }, 2000);
      },
      error: (err: any) => {
        this.error = err.error || "预约失败，请稍后重试";
        this.submitting = false;
      },
    });
  }

  private formatDate(date: Date): string {
    return date.toISOString().split("T")[0];
  }

  getTimeSlotDisplay(slot: string): string {
    return slot === "MORNING" ? "上午 (8:00-12:00)" : "下午 (14:00-17:30)";
  }

  formatDateDisplay(dateStr: string): string {
    const date = new Date(dateStr);
    const weekDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
    return `${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`;
  }
}
