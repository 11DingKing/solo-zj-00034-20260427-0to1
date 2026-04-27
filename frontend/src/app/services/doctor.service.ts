import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Doctor {
  id: number;
  userId: number;
  departmentId: number;
  name: string;
  title: string;
  specialty: string;
  introduction: string;
  avatar: string;
  status: string;
  department?: {
    id: number;
    name: string;
  };
}

const API_URL = "/api/doctors/";

@Injectable({
  providedIn: "root",
})
export class DoctorService {
  constructor(private http: HttpClient) {}

  getPublicDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(API_URL + "public/list");
  }

  getByDepartment(departmentId: number): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(
      API_URL + "public/department/" + departmentId,
    );
  }

  getPublicDoctor(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(API_URL + "public/" + id);
  }

  getAllDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(API_URL);
  }

  getDoctor(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(API_URL + id);
  }

  create(data: any): Observable<Doctor> {
    return this.http.post<Doctor>(API_URL, data);
  }

  update(id: number, doctor: Doctor): Observable<Doctor> {
    return this.http.put<Doctor>(API_URL + id, doctor);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(API_URL + id);
  }
}
