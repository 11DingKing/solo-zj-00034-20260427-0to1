import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "./services/auth.guard";

import { HomeComponent } from "./components/home/home.component";
import { LoginComponent } from "./components/login/login.component";
import { RegisterComponent } from "./components/register/register.component";

import { PatientDepartmentsComponent } from "./components/patient/departments/patient-departments.component";
import { PatientAppointmentsComponent } from "./components/patient/appointments/patient-appointments.component";
import { SymptomGuideComponent } from "./components/patient/symptom/symptom-guide.component";

import { DoctorAppointmentsComponent } from "./components/doctor/appointments/doctor-appointments.component";

import { AdminDashboardComponent } from "./components/admin/dashboard/admin-dashboard.component";
import { AdminDepartmentsComponent } from "./components/admin/departments/admin-departments.component";
import { AdminDoctorsComponent } from "./components/admin/doctors/admin-doctors.component";
import { AdminSchedulesComponent } from "./components/admin/schedules/admin-schedules.component";

const routes: Routes = [
  { path: "", component: HomeComponent },
  { path: "login", component: LoginComponent },
  { path: "register", component: RegisterComponent },

  // 患者端路由
  {
    path: "patient/departments",
    component: PatientDepartmentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["PATIENT"] },
  },
  {
    path: "patient/departments/:deptId",
    component: PatientDepartmentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["PATIENT"] },
  },
  {
    path: "patient/appointments",
    component: PatientAppointmentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["PATIENT"] },
  },
  {
    path: "patient/symptom",
    component: SymptomGuideComponent,
    canActivate: [AuthGuard],
    data: { roles: ["PATIENT"] },
  },

  // 医生端路由
  {
    path: "doctor/appointments",
    component: DoctorAppointmentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["DOCTOR"] },
  },

  // 管理员端路由
  {
    path: "admin/dashboard",
    component: AdminDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
  },
  {
    path: "admin/departments",
    component: AdminDepartmentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
  },
  {
    path: "admin/doctors",
    component: AdminDoctorsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
  },
  {
    path: "admin/schedules",
    component: AdminSchedulesComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
  },

  // 其他路由
  { path: "**", redirectTo: "" },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
