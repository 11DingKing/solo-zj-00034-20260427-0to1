import { Component, OnInit } from "@angular/core";
import {
  DashboardService,
  DashboardStats,
} from "../../services/dashboard.service";

@Component({
  selector: "app-admin-dashboard",
  templateUrl: "./admin-dashboard.component.html",
  styleUrls: ["./admin-dashboard.component.css"],
})
export class AdminDashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  calendarView: any = null;
  loading = true;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadCalendarView();
  }

  loadStats(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  loadCalendarView(): void {
    this.dashboardService.getCalendarView().subscribe({
      next: (data) => {
        this.calendarView = data;
      },
    });
  }

  getIntensityClass(intensity: string): string {
    const classes: { [key: string]: string } = {
      high: "intensity-high",
      medium: "intensity-medium",
      low: "intensity-low",
      none: "intensity-none",
    };
    return classes[intensity] || "intensity-none";
  }

  formatRate(rate: number): string {
    return (rate * 100).toFixed(0) + "%";
  }
}
