import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Appointment {
  id: number;
  appointmentNo: string;
  patientId: number;
  doctorId: number;
  doctorName?: string;
  departmentName?: string;
  appointmentDate: string;
  timeSlot: string;
  queueNumber: number;
  patientName: string;
  patientIdCard?: string;
  patientPhone?: string;
  symptoms?: string;
  status: string;
  createdAt: string;
}

export interface AppointmentRequest {
  doctorId: number;
  appointmentDate: string;
  timeSlot: string;
  patientName: string;
  patientIdCard?: string;
  patientPhone?: string;
  symptoms?: string;
}

const API_URL = "/api/appointments/";

@Injectable({
  providedIn: "root",
})
export class AppointmentService {
  constructor(private http: HttpClient) {}

  create(request: AppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>(API_URL, request);
  }

  getMyAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(API_URL + "my");
  }

  cancel(id: number, reason?: string): Observable<void> {
    return this.http.post<void>(`${API_URL}${id}/cancel`, { reason });
  }

  getDoctorTodayAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(API_URL + "doctor/today");
  }

  getDoctorUpcomingAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(API_URL + "doctor/upcoming");
  }

  getDoctorAppointmentsByDate(date: string): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${API_URL}doctor/date/${date}`);
  }

  markAsVisited(id: number): Observable<void> {
    return this.http.post<void>(`${API_URL}${id}/visited`, {});
  }

  markAsNoShow(id: number): Observable<void> {
    return this.http.post<void>(`${API_URL}${id}/no-show`, {});
  }

  getAppointment(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(API_URL + id);
  }
}
