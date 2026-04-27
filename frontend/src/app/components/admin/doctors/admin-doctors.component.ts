import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { DoctorService, Doctor } from "../../services/doctor.service";
import {
  DepartmentService,
  Department,
} from "../../services/department.service";

@Component({
  selector: "app-admin-doctors",
  templateUrl: "./admin-doctors.component.html",
  styleUrls: ["./admin-doctors.component.css"],
})
export class AdminDoctorsComponent implements OnInit {
  doctors: Doctor[] = [];
  departments: Department[] = [];
  loading = true;
  submitting = false;
  error = "";
  success = "";

  showModal = false;
  editingDoctor: Doctor | null = null;
  doctorForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private departmentService: DepartmentService,
  ) {
    this.doctorForm = this.fb.group({
      name: ["", Validators.required],
      title: ["", Validators.required],
      departmentId: ["", Validators.required],
      specialty: ["", Validators.required],
      introduction: [""],
      avatar: [""],
      status: ["AVAILABLE"],
      username: [""],
      password: [""],
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.departmentService.getAllDepartments().subscribe({
      next: (data) => {
        this.departments = data;
      },
      error: () => {
        this.error = "加载科室列表失败";
      },
    });

    this.doctorService.getAllDoctors().subscribe({
      next: (data) => {
        this.doctors = data;
        this.loading = false;
      },
      error: () => {
        this.error = "加载医生列表失败";
        this.loading = false;
      },
    });
  }

  openAddModal(): void {
    this.editingDoctor = null;
    this.doctorForm.reset({
      name: "",
      title: "",
      departmentId: "",
      specialty: "",
      introduction: "",
      avatar: "",
      status: "AVAILABLE",
      username: "",
      password: "",
    });
    this.showModal = true;
    this.error = "";
    this.success = "";
  }

  openEditModal(doctor: Doctor): void {
    this.editingDoctor = doctor;
    this.doctorForm.patchValue({
      name: doctor.name,
      title: doctor.title || "",
      departmentId: doctor.departmentId,
      specialty: doctor.specialty || "",
      introduction: doctor.introduction || "",
      avatar: doctor.avatar || "",
      status: doctor.status,
      username: "",
      password: "",
    });
    this.showModal = true;
    this.error = "";
    this.success = "";
  }

  closeModal(): void {
    this.showModal = false;
    this.editingDoctor = null;
  }

  onSubmit(): void {
    if (this.editingDoctor) {
      if (
        this.doctorForm.get("name")?.invalid ||
        this.doctorForm.get("title")?.invalid ||
        this.doctorForm.get("departmentId")?.invalid ||
        this.doctorForm.get("specialty")?.invalid
      ) {
        return;
      }
    } else {
      if (this.doctorForm.invalid) {
        return;
      }
    }

    this.submitting = true;
    this.error = "";

    const formValue = this.doctorForm.value;

    if (this.editingDoctor) {
      const doctor: Doctor = {
        id: this.editingDoctor.id,
        userId: this.editingDoctor.userId,
        name: formValue.name,
        title: formValue.title,
        departmentId: formValue.departmentId,
        specialty: formValue.specialty,
        introduction: formValue.introduction,
        avatar: formValue.avatar,
        status: formValue.status,
      };

      this.doctorService.update(this.editingDoctor.id, doctor).subscribe({
        next: () => {
          this.success = "更新成功";
          this.submitting = false;
          this.closeModal();
          this.loadData();
          setTimeout(() => (this.success = ""), 3000);
        },
        error: (err) => {
          this.error = err.error || "更新失败";
          this.submitting = false;
        },
      });
    } else {
      const data = {
        name: formValue.name,
        title: formValue.title,
        departmentId: formValue.departmentId,
        specialty: formValue.specialty,
        introduction: formValue.introduction,
        avatar: formValue.avatar,
        username: formValue.username,
        password: formValue.password,
      };

      this.doctorService.create(data).subscribe({
        next: () => {
          this.success = "创建成功";
          this.submitting = false;
          this.closeModal();
          this.loadData();
          setTimeout(() => (this.success = ""), 3000);
        },
        error: (err) => {
          this.error = err.error || "创建失败";
          this.submitting = false;
        },
      });
    }
  }

  deleteDoctor(doctor: Doctor): void {
    if (!confirm(`确定要删除医生「${doctor.name}」吗？删除后无法恢复。`)) {
      return;
    }

    this.doctorService.delete(doctor.id).subscribe({
      next: () => {
        this.success = "删除成功";
        this.loadData();
        setTimeout(() => (this.success = ""), 3000);
      },
      error: (err) => {
        this.error = err.error || "删除失败";
      },
    });
  }

  getDepartmentName(deptId: number): string {
    const dept = this.departments.find((d) => d.id === deptId);
    return dept ? dept.name : "";
  }

  getStatusBadgeClass(status: string): string {
    return status === "AVAILABLE" ? "badge-pending" : "badge-cancelled";
  }

  getStatusText(status: string): string {
    return status === "AVAILABLE" ? "出诊中" : "停诊";
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
    return icons[name] || "👨‍⚕️";
  }
}
