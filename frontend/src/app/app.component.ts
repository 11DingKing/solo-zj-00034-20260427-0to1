import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService, User } from "./services/auth.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"],
})
export class AppComponent {
  title = "医院门诊预约挂号系统";
  currentUser: User | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(["/"]);
  }

  isPatient(): boolean {
    return this.authService.isPatient();
  }

  isDoctor(): boolean {
    return this.authService.isDoctor();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}
