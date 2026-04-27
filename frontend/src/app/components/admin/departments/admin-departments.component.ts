import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DepartmentService, Department } from '../../services/department.service';

@Component({
  selector: 'app-admin-departments',
  templateUrl: './admin-departments.component.html',
  styleUrls: ['./admin-departments.component.css']
})
export class AdminDepartmentsComponent implements OnInit {
  departments: Department[] = [];
  loading = true;
  submitting = false;
  error = '';
  success = '';

  showModal = false;
  editingDepartment: Department | null = null;
  departmentForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private departmentService: DepartmentService
  ) {
    this.departmentForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      floorLocation: ['', Validators.required],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.loading = true;
    this.departmentService.getAllDepartments().subscribe({
      next: (data) => {
        this.departments = data;
        this.loading = false;
      },
      error: () => {
        this.error = '加载科室列表失败';
        this.loading = false;
      }
    });
  }

  openAddModal(): void {
    this.editingDepartment = null;
    this.departmentForm.reset({
      name: '',
      description: '',
      floorLocation: '',
      isActive: true
    });
    this.showModal = true;
    this.error = '';
    this.success = '';
  }

  openEditModal(dept: Department): void {
    this.editingDepartment = dept;
    this.departmentForm.patchValue({
      name: dept.name,
      description: dept.description || '',
      floorLocation: dept.floorLocation || '',
      isActive: dept.isActive
    });
    this.showModal = true;
    this.error = '';
    this.success = '';
  }

  closeModal(): void {
    this.showModal = false;
    this.editingDepartment = null;
  }

  onSubmit(): void {
    if (this.departmentForm.invalid) {
      return;
    }

    this.submitting = true;
    this.error = '';

    const department: Department = {
      id: this.editingDepartment?.id || 0,
      name: this.departmentForm.value.name,
      description: this.departmentForm.value.description,
      floorLocation: this.departmentForm.value.floorLocation,
      isActive: this.departmentForm.value.isActive
    };

    if (this.editingDepartment) {
      this.departmentService.update(this.editingDepartment.id, department).subscribe({
        next: () => {
          this.success = '更新成功';
          this.submitting = false;
          this.closeModal();
          this.loadDepartments();
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = err.error || '更新失败';
          this.submitting = false;
        }
      });
    } else {
      this.departmentService.create(department).subscribe({
        next: () => {
          this.success = '创建成功';
          this.submitting = false;
          this.closeModal();
          this.loadDepartments();
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = err.error || '创建失败';
          this.submitting = false;
        }
      });
    }
  }

  deleteDepartment(dept: Department): void {
    if (!confirm(`确定要删除科室「${dept.name}」吗？删除后无法恢复。`)) {
      return;
    }

    this.departmentService.delete(dept.id).subscribe({
      next: () => {
        this.success = '删除成功';
        this.loadDepartments();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = err.error || '删除失败，该科室可能有关联数据';
      }
    });
  }

  getStatusBadgeClass(isActive: boolean): string {
    return isActive ? 'badge-pending' : 'badge-cancelled';
  }

  getStatusText(isActive: boolean): string {
    return isActive ? '启用' : '停用';
  }
}
