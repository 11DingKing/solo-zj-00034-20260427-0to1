import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Department {
  id: number;
  name: string;
  description: string;
  floorLocation: string;
  isActive: boolean;
}

const API_URL = "/api/departments/";

@Injectable({
  providedIn: "root",
})
export class DepartmentService {
  constructor(private http: HttpClient) {}

  getPublicDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(API_URL + "public/list");
  }

  getPublicDepartment(id: number): Observable<Department> {
    return this.http.get<Department>(API_URL + "public/" + id);
  }

  getAllDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(API_URL);
  }

  getDepartment(id: number): Observable<Department> {
    return this.http.get<Department>(API_URL + id);
  }

  create(department: Department): Observable<Department> {
    return this.http.post<Department>(API_URL, department);
  }

  update(id: number, department: Department): Observable<Department> {
    return this.http.put<Department>(API_URL + id, department);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(API_URL + id);
  }
}
