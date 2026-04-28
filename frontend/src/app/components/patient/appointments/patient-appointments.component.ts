import { Component, OnInit } from "@angular/core";
import {
  AppointmentService,
  Appointment,
} from "../../../services/appointment.service";

@Component({
  selector: "app-patient-appointments",
  templateUrl: "./patient-appointments.component.html",
  styleUrls: ["./patient-appointments.component.css"],
})
export class PatientAppointmentsComponent implements OnInit {
  appointments: Appointment[] = [];
  loading = true;
  error = "";
  success = "";
  cancellingId: number | null = null;

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.loading = true;
    this.appointmentService.getMyAppointments().subscribe({
      next: (data: Appointment[]) => {
        this.appointments = data.sort(
          (a: Appointment, b: Appointment) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
        );
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

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const weekDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`;
  }

  canCancel(appointment: Appointment): boolean {
    if (appointment.status !== "PENDING") {
      return false;
    }

    const apptDate = new Date(appointment.appointmentDate);
    const now = new Date();

    const cutoffHour = appointment.timeSlot === "MORNING" ? 8 : 14;
    const cutoffTime = new Date(apptDate);
    cutoffTime.setHours(cutoffHour, 0, 0, 0);
    cutoffTime.setTime(cutoffTime.getTime() - 2 * 60 * 60 * 1000);

    return now < cutoffTime;
  }

  cancelAppointment(appointment: Appointment): void {
    if (!confirm("确定要取消该预约吗？")) {
      return;
    }

    this.cancellingId = appointment.id;
    this.error = "";
    this.success = "";

    this.appointmentService.cancel(appointment.id, "用户主动取消").subscribe({
      next: () => {
        this.success = "预约已取消";
        this.cancellingId = null;
        appointment.status = "CANCELLED";
        setTimeout(() => (this.success = ""), 3000);
      },
      error: (err: any) => {
        this.error = err.error || "取消失败，请稍后重试";
        this.cancellingId = null;
      },
    });
  }
}
