import { Component, OnInit } from "@angular/core";
import { DoctorService, Doctor } from "../../services/doctor.service";
import {
  ScheduleService,
  ScheduleTemplate,
} from "../../services/schedule.service";
import {
  DepartmentService,
  Department,
} from "../../services/department.service";

interface DaySchedule {
  dayOfWeek: number;
  dayName: string;
  morning: SlotSchedule;
  afternoon: SlotSchedule;
}

interface SlotSchedule {
  isActive: boolean;
  maxAppointments: number;
  templateId?: number;
}

@Component({
  selector: "app-admin-schedules",
  templateUrl: "./admin-schedules.component.html",
  styleUrls: ["./admin-schedules.component.css"],
})
export class AdminSchedulesComponent implements OnInit {
  doctors: Doctor[] = [];
  departments: Department[] = [];
  loading = true;
  submitting = false;
  error = "";
  success = "";

  selectedDoctor: Doctor | null = null;
  doctorSchedule: DaySchedule[] = [];

  weekDays = [
    { dayOfWeek: 1, name: "周一" },
    { dayOfWeek: 2, name: "周二" },
    { dayOfWeek: 3, name: "周三" },
    { dayOfWeek: 4, name: "周四" },
    { dayOfWeek: 5, name: "周五" },
    { dayOfWeek: 6, name: "周六" },
    { dayOfWeek: 7, name: "周日" },
  ];

  constructor(
    private doctorService: DoctorService,
    private scheduleService: ScheduleService,
    private departmentService: DepartmentService,
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.departmentService.getAllDepartments().subscribe({
      next: (data) => {
        this.departments = data;
      },
      error: () => {
        this.error = "加载科室列表失败";
      },
    });

    this.doctorService.getAllDoctors().subscribe({
      next: (data) => {
        this.doctors = data;
        this.loading = false;
      },
      error: () => {
        this.error = "加载医生列表失败";
        this.loading = false;
      },
    });
  }

  selectDoctor(doctor: Doctor): void {
    this.selectedDoctor = doctor;
    this.error = "";
    this.success = "";
    this.loadDoctorSchedule(doctor.id);
  }

  loadDoctorSchedule(doctorId: number): void {
    this.scheduleService.getTemplates(doctorId).subscribe({
      next: (templates) => {
        this.buildScheduleFromTemplates(templates);
      },
      error: () => {
        this.error = "加载排班模板失败";
      },
    });
  }

  buildScheduleFromTemplates(templates: ScheduleTemplate[]): void {
    this.doctorSchedule = this.weekDays.map((day) => {
      const morningTemplate = templates.find(
        (t) => t.dayOfWeek === day.dayOfWeek && t.timeSlot === "MORNING",
      );
      const afternoonTemplate = templates.find(
        (t) => t.dayOfWeek === day.dayOfWeek && t.timeSlot === "AFTERNOON",
      );

      return {
        dayOfWeek: day.dayOfWeek,
        dayName: day.name,
        morning: {
          isActive: morningTemplate ? morningTemplate.isActive : false,
          maxAppointments: morningTemplate
            ? morningTemplate.maxAppointments
            : 10,
          templateId: morningTemplate?.id,
        },
        afternoon: {
          isActive: afternoonTemplate ? afternoonTemplate.isActive : false,
          maxAppointments: afternoonTemplate
            ? afternoonTemplate.maxAppointments
            : 10,
          templateId: afternoonTemplate?.id,
        },
      };
    });
  }

  toggleSlot(daySchedule: DaySchedule, slot: "morning" | "afternoon"): void {
    daySchedule[slot].isActive = !daySchedule[slot].isActive;
  }

  updateMaxAppointments(
    daySchedule: DaySchedule,
    slot: "morning" | "afternoon",
    value: string,
  ): void {
    const numValue = parseInt(value, 10);
    if (!isNaN(numValue) && numValue > 0) {
      daySchedule[slot].maxAppointments = numValue;
    }
  }

  saveSchedule(): void {
    if (!this.selectedDoctor) {
      return;
    }

    this.submitting = true;
    this.error = "";

    const savePromises: Promise<any>[] = [];

    for (const daySchedule of this.doctorSchedule) {
      for (const slot of ["morning", "afternoon"] as const) {
        const slotData = daySchedule[slot];
        const timeSlot = slot === "morning" ? "MORNING" : "AFTERNOON";

        if (slotData.templateId) {
          savePromises.push(
            this.scheduleService
              .updateTemplate(slotData.templateId, {
                id: slotData.templateId,
                doctorId: this.selectedDoctor.id,
                dayOfWeek: daySchedule.dayOfWeek,
                timeSlot: timeSlot,
                maxAppointments: slotData.maxAppointments,
                isActive: slotData.isActive,
              })
              .toPromise(),
          );
        } else if (slotData.isActive) {
          savePromises.push(
            this.scheduleService
              .createTemplate({
                doctorId: this.selectedDoctor.id,
                dayOfWeek: daySchedule.dayOfWeek,
                timeSlot: timeSlot,
                maxAppointments: slotData.maxAppointments,
                isActive: slotData.isActive,
              })
              .toPromise(),
          );
        }
      }
    }

    Promise.all(savePromises)
      .then(() => {
        this.success = "排班保存成功";
        this.submitting = false;
        if (this.selectedDoctor) {
          this.loadDoctorSchedule(this.selectedDoctor.id);
        }
        setTimeout(() => (this.success = ""), 3000);
      })
      .catch(() => {
        this.error = "保存失败，请稍后重试";
        this.submitting = false;
      });
  }

  copyWeekToNext(): void {
    if (!this.selectedDoctor) {
      return;
    }

    if (!confirm("确定要把本周排班复制到下周吗？")) {
      return;
    }

    const today = new Date();
    const currentMonday = new Date(today);
    const day = today.getDay();
    const diff = today.getDate() - day + (day === 0 ? -6 : 1);
    currentMonday.setDate(diff);
    currentMonday.setHours(0, 0, 0, 0);

    const nextMonday = new Date(currentMonday);
    nextMonday.setDate(nextMonday.getDate() + 7);

    const formatDate = (date: Date) => date.toISOString().split("T")[0];

    this.scheduleService
      .copyWeek({
        doctorId: this.selectedDoctor.id,
        fromWeekStart: formatDate(currentMonday),
        toWeekStart: formatDate(nextMonday),
      })
      .subscribe({
        next: () => {
          this.success = "排班已复制到下周";
          setTimeout(() => (this.success = ""), 3000);
        },
        error: () => {
          this.error = "复制失败，请稍后重试";
        },
      });
  }

  getDepartmentName(deptId: number): string {
    const dept = this.departments.find((d) => d.id === deptId);
    return dept ? dept.name : "";
  }

  getSlotClass(slot: SlotSchedule): string {
    if (!slot.isActive) {
      return "slot-inactive";
    }
    return "slot-active";
  }
}
