import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
} from "@angular/core";
import { Chart, registerables } from "chart.js";
import {
  DashboardService,
  DashboardStats,
} from "../../../services/dashboard.service";

Chart.register(...registerables);

@Component({
  selector: "app-admin-dashboard",
  templateUrl: "./admin-dashboard.component.html",
  styleUrls: ["./admin-dashboard.component.css"],
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {
  stats: DashboardStats | null = null;
  calendarView: any = null;
  loading = true;

  @ViewChild("trendChart") trendChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild("workloadChart") workloadChartRef!: ElementRef<HTMLCanvasElement>;

  private trendChart: Chart | null = null;
  private workloadChart: Chart | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadCalendarView();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initTrendChart();
      this.initWorkloadChart();
    }, 100);
  }

  loadStats(): void {
    this.dashboardService.getStats().subscribe({
      next: (data: DashboardStats) => {
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
      next: (data: any) => {
        this.calendarView = data;
      },
    });
  }

  private initTrendChart(): void {
    if (!this.trendChartRef) return;

    const ctx = this.trendChartRef.nativeElement.getContext("2d");
    if (!ctx) return;

    const labels = this.generateLast30DaysLabels();
    const data = this.generateMockTrendData();

    this.trendChart = new Chart(ctx, {
      type: "line",
      data: {
        labels: labels,
        datasets: [
          {
            label: "预约数",
            data: data,
            borderColor: "#1890ff",
            backgroundColor: "rgba(24, 144, 255, 0.1)",
            fill: true,
            tension: 0.4,
            pointRadius: 3,
            pointHoverRadius: 5,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false,
          },
          title: {
            display: true,
            text: "近30天预约趋势",
            font: {
              size: 16,
              weight: "bold",
            },
            padding: {
              bottom: 20,
            },
          },
        },
        scales: {
          x: {
            grid: {
              display: false,
            },
            ticks: {
              maxTicksLimit: 10,
            },
          },
          y: {
            beginAtZero: true,
            grid: {
              color: "rgba(0, 0, 0, 0.05)",
            },
          },
        },
      },
    });
  }

  private initWorkloadChart(): void {
    if (!this.workloadChartRef) return;

    const ctx = this.workloadChartRef.nativeElement.getContext("2d");
    if (!ctx) return;

    const labels = ["张医生", "李医生", "王医生", "刘医生", "陈医生"];
    const data = [156, 142, 128, 95, 78];
    const colors = ["#1890ff", "#52c41a", "#faad14", "#722ed1", "#eb2f96"];

    this.workloadChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [
          {
            label: "预约数量",
            data: data,
            backgroundColor: colors,
            borderRadius: 6,
            barThickness: 40,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false,
          },
          title: {
            display: true,
            text: "医生工作量排行",
            font: {
              size: 16,
              weight: "bold",
            },
            padding: {
              bottom: 20,
            },
          },
        },
        scales: {
          x: {
            grid: {
              display: false,
            },
          },
          y: {
            beginAtZero: true,
            grid: {
              color: "rgba(0, 0, 0, 0.05)",
            },
          },
        },
      },
    });
  }

  private generateLast30DaysLabels(): string[] {
    const labels: string[] = [];
    const today = new Date();

    for (let i = 29; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      labels.push(`${date.getMonth() + 1}/${date.getDate()}`);
    }

    return labels;
  }

  private generateMockTrendData(): number[] {
    const data: number[] = [];
    for (let i = 0; i < 30; i++) {
      data.push(Math.floor(Math.random() * 30) + 10);
    }
    return data;
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
