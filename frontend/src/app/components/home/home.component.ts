import { Component, OnInit } from "@angular/core";
import { DepartmentService, Department } from "../services/department.service";
import { AuthService } from "../services/auth.service";

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.css"],
})
export class HomeComponent implements OnInit {
  departments: Department[] = [];
  loading = true;

  constructor(
    private departmentService: DepartmentService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.departmentService.getPublicDepartments().subscribe({
      next: (data) => {
        this.departments = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isPatient(): boolean {
    return this.authService.isPatient();
  }

  getDeptIcon(name: string): string {
    const icons: { [key: string]: string } = {
      内科: "🫀",
      外科: "🔪",
      儿科: "👶",
      妇产科: "🤰",
      眼科: "👁️",
      耳鼻喉科: "👂",
      皮肤科: "🧴",
      口腔科: "🦷",
    };
    return icons[name] || "🏥";
  }
}
