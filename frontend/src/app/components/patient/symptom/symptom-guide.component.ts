import { Component } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { SymptomService } from "../../services/dashboard.service";
import { Department } from "../../services/department.service";
import { Router } from "@angular/router";

@Component({
  selector: "app-symptom-guide",
  templateUrl: "./symptom-guide.component.html",
  styleUrls: ["./symptom-guide.component.css"],
})
export class SymptomGuideComponent {
  symptomForm: FormGroup;
  recommendedDepartments: Department[] = [];
  loading = false;
  searched = false;

  quickSymptoms = [
    "头痛 发烧",
    "咳嗽 感冒",
    "腹痛 腹泻",
    "皮肤瘙痒 皮疹",
    "眼睛不适",
    "牙疼 牙龈肿痛",
  ];

  constructor(
    private fb: FormBuilder,
    private symptomService: SymptomService,
    private router: Router,
  ) {
    this.symptomForm = this.fb.group({
      symptoms: ["", Validators.required],
    });
  }

  onSubmit(): void {
    if (this.symptomForm.invalid) {
      return;
    }

    this.loading = true;
    this.searched = true;
    this.recommendedDepartments = [];

    const symptoms = this.symptomForm.value.symptoms;

    this.symptomService.recommendDepartments(symptoms).subscribe({
      next: (data) => {
        this.recommendedDepartments = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  quickSelect(symptom: string): void {
    this.symptomForm.patchValue({ symptoms: symptom });
  }

  selectDepartment(dept: Department): void {
    this.router.navigate(["/patient/departments", dept.id]);
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
