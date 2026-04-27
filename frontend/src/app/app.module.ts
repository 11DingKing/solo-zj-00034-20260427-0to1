import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";

// Services
import { AuthInterceptor } from "./services/auth.interceptor";

// Components
import { HomeComponent } from "./components/home/home.component";
import { LoginComponent } from "./components/login/login.component";
import { RegisterComponent } from "./components/register/register.component";

// Patient Components
import { PatientDepartmentsComponent } from "./components/patient/departments/patient-departments.component";
import { PatientAppointmentsComponent } from "./components/patient/appointments/patient-appointments.component";
import { SymptomGuideComponent } from "./components/patient/symptom/symptom-guide.component";

// Doctor Components
import { DoctorAppointmentsComponent } from "./components/doctor/appointments/doctor-appointments.component";

// Admin Components
import { AdminDashboardComponent } from "./components/admin/dashboard/admin-dashboard.component";
import { AdminDepartmentsComponent } from "./components/admin/departments/admin-departments.component";
import { AdminDoctorsComponent } from "./components/admin/doctors/admin-doctors.component";
import { AdminSchedulesComponent } from "./components/admin/schedules/admin-schedules.component";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    PatientDepartmentsComponent,
    PatientAppointmentsComponent,
    SymptomGuideComponent,
    DoctorAppointmentsComponent,
    AdminDashboardComponent,
    AdminDepartmentsComponent,
    AdminDoctorsComponent,
    AdminSchedulesComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
