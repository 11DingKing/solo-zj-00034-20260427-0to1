import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Department } from "./department.service";

export interface DashboardStats {
  todayAppointments: number;
  last30DaysAppointments: number;
  noShowRate: number;
  departmentStats: DepartmentStats[];
  doctorStats: DoctorStats[];
  dailyTrends: DailyTrend[];
  calendarView: any;
}

export interface DepartmentStats {
  departmentId: number;
  departmentName: string;
  count: number;
}

export interface DoctorStats {
  doctorId: number;
  doctorName: string;
  departmentName: string;
  count: number;
}

export interface DailyTrend {
  date: string;
  count: number;
}

const API_URL = "/api/admin/";

@Injectable({
  providedIn: "root",
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(API_URL + "dashboard");
  }

  getCalendarView(weekStart?: string): Observable<any> {
    const url = weekStart
      ? `${API_URL}calendar-view?weekStart=${weekStart}`
      : API_URL + "calendar-view";
    return this.http.get<any>(url);
  }
}

@Injectable({
  providedIn: "root",
})
export class SymptomService {
  constructor(private http: HttpClient) {}

  recommendDepartments(symptoms: string): Observable<Department[]> {
    return this.http.post<Department[]>("/api/symptoms/recommend", {
      symptoms,
    });
  }
}
