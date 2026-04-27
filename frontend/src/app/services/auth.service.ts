import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";

export interface User {
  id: number;
  username: string;
  realName: string;
  role: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  idCard?: string;
  phone?: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  realName: string;
  role: string;
}

const API_URL = "/api/auth/";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem("currentUser");
    const initialUser = storedUser ? JSON.parse(storedUser) : null;
    this.currentUserSubject = new BehaviorSubject<User | null>(initialUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem("token");
  }

  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(API_URL + "login", request).pipe(
      tap((response) => {
        localStorage.setItem("token", response.token);
        const user: User = {
          id: response.id,
          username: response.username,
          realName: response.realName,
          role: response.role,
        };
        localStorage.setItem("currentUser", JSON.stringify(user));
        this.currentUserSubject.next(user);
      }),
    );
  }

  register(request: RegisterRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(API_URL + "register", request).pipe(
      tap((response) => {
        localStorage.setItem("token", response.token);
        const user: User = {
          id: response.id,
          username: response.username,
          realName: response.realName,
          role: response.role,
        };
        localStorage.setItem("currentUser", JSON.stringify(user));
        this.currentUserSubject.next(user);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.currentUserValue;
  }

  isPatient(): boolean {
    return this.currentUserValue?.role === "PATIENT";
  }

  isDoctor(): boolean {
    return this.currentUserValue?.role === "DOCTOR";
  }

  isAdmin(): boolean {
    return this.currentUserValue?.role === "ADMIN";
  }
}
