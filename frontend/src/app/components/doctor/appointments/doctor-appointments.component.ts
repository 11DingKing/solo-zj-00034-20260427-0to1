import { Component, OnInit } from "@angular/core";
import {
  AppointmentService,
  Appointment,
} from "../../../services/appointment.service";

@Component({
  selector: "app-doctor-appointments",
  templateUrl: "./doctor-appointments.component.html",
  styleUrls: ["./doctor-appointments.component.css"],
})
export class DoctorAppointmentsComponent implements OnInit {
  todayAppointments: Appointment[] = [];
  upcomingAppointments: Appointment[] = [];
  loading = true;
  error = "";
  success = "";

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.loading = true;

    this.appointmentService.getDoctorTodayAppointments().subscribe({
      next: (data: Appointment[]) => {
        this.todayAppointments = data;
      },
      error: () => {
        this.error = "加载今日预约失败";
      },
    });

    this.appointmentService.getDoctorUpcomingAppointments().subscribe({
      next: (data: Appointment[]) => {
        this.upcomingAppointments = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = "加载预约列表失败";
      },
    });
  }

  getStatusBadgeClass(status: string): string {
    const classes: { [key: string]: string } = {
      PENDING: "badge-pending",
      VISITED: "badge-visited",
      NO_SHOW: "badge-no-show",
      CANCELLED: "badge-cancelled",
    };
    return classes[status] || "badge-pending";
  }

  getStatusText(status: string): string {
    const texts: { [key: string]: string } = {
      PENDING: "待就诊",
      VISITED: "已就诊",
      NO_SHOW: "爽约",
      CANCELLED: "已取消",
    };
    return texts[status] || status;
  }

  getTimeSlotDisplay(slot: string): string {
    return slot === "MORNING" ? "上午 (8:00-12:00)" : "下午 (14:00-17:30)";
  }

  markAsVisited(appointment: Appointment): void {
    if (!confirm("确认标记该患者为已就诊？")) {
      return;
    }

    this.appointmentService.markAsVisited(appointment.id).subscribe({
      next: () => {
        this.success = "标记成功";
        appointment.status = "VISITED";
        setTimeout(() => (this.success = ""), 3000);
      },
      error: (err: any) => {
        this.error = err.error || "操作失败";
      },
    });
  }

  markAsNoShow(appointment: Appointment): void {
    if (!confirm("确认标记该患者为爽约？连续3次爽约将被限制预约30天。")) {
      return;
    }

    this.appointmentService.markAsNoShow(appointment.id).subscribe({
      next: () => {
        this.success = "标记成功";
        appointment.status = "NO_SHOW";
        setTimeout(() => (this.success = ""), 3000);
      },
      error: (err: any) => {
        this.error = err.error || "操作失败";
      },
    });
  }
}
