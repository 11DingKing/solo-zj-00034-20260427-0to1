import { Component } from "@angular/core";
import { Router } from "@angular/router";
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from "@angular/forms";
import { AuthService, RegisterRequest } from "../../services/auth.service";

@Component({
  selector: "app-register",
  templateUrl: "./register.component.html",
  styleUrls: ["./register.component.css"],
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error = "";

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.registerForm = this.fb.group(
      {
        username: [
          "",
          [
            Validators.required,
            Validators.minLength(3),
            Validators.maxLength(50),
          ],
        ],
        password: [
          "",
          [
            Validators.required,
            Validators.minLength(6),
            Validators.maxLength(50),
          ],
        ],
        confirmPassword: ["", Validators.required],
        realName: ["", Validators.required],
        idCard: ["", [Validators.pattern(/^\d{17}[\dXx]$/)]],
        phone: ["", [Validators.pattern(/^1[3-9]\d{9}$/)]],
      },
      { validators: this.passwordMatchValidator },
    );
  }

  passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get("password");
    const confirmPassword = form.get("confirmPassword");

    if (
      password &&
      confirmPassword &&
      password.value !== confirmPassword.value
    ) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = "";

    const request: RegisterRequest = {
      username: this.registerForm.value.username,
      password: this.registerForm.value.password,
      realName: this.registerForm.value.realName,
      idCard: this.registerForm.value.idCard || undefined,
      phone: this.registerForm.value.phone || undefined,
    };

    this.authService.register(request).subscribe({
      next: () => {
        this.router.navigate(["/"]);
      },
      error: (err) => {
        this.error = err.error || "注册失败，请稍后重试";
        this.loading = false;
      },
    });
  }
}
