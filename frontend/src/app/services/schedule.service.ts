import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Schedule {
  id: number;
  doctorId: number;
  scheduleDate: string;
  timeSlot: string;
  maxAppointments: number;
  bookedCount: number;
  remainingSlots: number;
  isActive: boolean;
  isTemporaryAdjusted: boolean;
}

export interface ScheduleTemplate {
  id?: number;
  doctorId: number;
  dayOfWeek: number;
  timeSlot: string;
  maxAppointments: number;
  isActive: boolean;
}

const API_URL = "/api/schedules/";

@Injectable({
  providedIn: "root",
})
export class ScheduleService {
  constructor(private http: HttpClient) {}

  getDoctorSchedules(
    doctorId: number,
    startDate: string,
    endDate: string,
  ): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(
      `${API_URL}public/doctor/${doctorId}?startDate=${startDate}&endDate=${endDate}`,
    );
  }

  getTemplates(doctorId: number): Observable<ScheduleTemplate[]> {
    return this.http.get<ScheduleTemplate[]>(
      `${API_URL}templates/doctor/${doctorId}`,
    );
  }

  createTemplate(template: ScheduleTemplate): Observable<ScheduleTemplate> {
    return this.http.post<ScheduleTemplate>(`${API_URL}templates`, template);
  }

  updateTemplate(
    id: number,
    template: ScheduleTemplate,
  ): Observable<ScheduleTemplate> {
    return this.http.put<ScheduleTemplate>(
      `${API_URL}templates/${id}`,
      template,
    );
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}templates/${id}`);
  }

  copyWeek(data: {
    doctorId: number;
    fromWeekStart: string;
    toWeekStart: string;
  }): Observable<void> {
    return this.http.post<void>(`${API_URL}copy-week`, data);
  }

  adjustSchedule(data: {
    doctorId: number;
    date: string;
    timeSlot: string;
    isActive?: boolean;
    maxAppointments?: number;
  }): Observable<void> {
    return this.http.post<void>(`${API_URL}adjust`, data);
  }
}
