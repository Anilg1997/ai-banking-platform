import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent implements OnInit, OnDestroy {
  registerForm: FormGroup;
  isLoading = false;
  hidePassword = true;
  hideConfirmPassword = true;
  passwordStrength = 0;
  passwordStrengthText = '';
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group(
      {
        firstName: ['', [Validators.required, Validators.maxLength(50)]],
        lastName: ['', [Validators.required, Validators.maxLength(50)]],
        username: [
          '',
          [
            Validators.required,
            Validators.minLength(3),
            Validators.maxLength(50),
            Validators.pattern(/^[a-zA-Z0-9_]+$/),
          ],
        ],
        email: ['', [Validators.required, Validators.email]],
        phoneNumber: ['', [Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
        password: [
          '',
          [
            Validators.required,
            Validators.minLength(8),
            Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/),
          ],
        ],
        confirmPassword: ['', [Validators.required]],
        agreeTerms: [false, [Validators.requiredTrue]],
      },
      { validators: this.passwordMatchValidator }
    );

    // Monitor password for strength
    this.registerForm.get('password')?.valueChanges.subscribe((value) => {
      this.updatePasswordStrength(value);
    });
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn) {
      this.router.navigate(['/dashboard']);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach((key) => {
        this.registerForm.get(key)?.markAsTouched();
      });

      if (this.registerForm.hasError('passwordMismatch')) {
        this.snackBar.open('Passwords do not match', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      }
      return;
    }

    this.isLoading = true;
    const { firstName, lastName, username, email, phoneNumber, password } =
      this.registerForm.value;

    this.authService
      .register({ firstName, lastName, username, email, phoneNumber, password })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          this.snackBar.open(
            `Welcome to NovaBank, ${response.user.firstName}! Your account is ready.`,
            'Close',
            {
              duration: 5000,
              panelClass: ['success-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            }
          );
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open(
            error.message || 'Registration failed. Please try again.',
            'Close',
            {
              duration: 5000,
              panelClass: ['error-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            }
          );
        },
      });
  }

  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);
    if (!control) return '';

    if (control.hasError('required')) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    }
    if (control.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (control.hasError('minlength')) {
      return `Must be at least ${control.getError('minlength').requiredLength} characters`;
    }
    if (control.hasError('maxlength')) {
      return `Must not exceed ${control.getError('maxlength').requiredLength} characters`;
    }
    if (control.hasError('pattern')) {
      if (field === 'username') {
        return 'Only letters, numbers, and underscores allowed';
      }
      if (field === 'password') {
        return 'Must include uppercase, lowercase, number, and special character';
      }
    }
    if (field === 'confirmPassword' && this.registerForm.hasError('passwordMismatch')) {
      return 'Passwords do not match';
    }
    return '';
  }

  private updatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength = 0;
      this.passwordStrengthText = '';
      return;
    }

    let strength = 0;
    if (password.length >= 8) strength += 20;
    if (password.length >= 12) strength += 10;
    if (/[a-z]/.test(password)) strength += 15;
    if (/[A-Z]/.test(password)) strength += 15;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[@#$%^&+=!]/.test(password)) strength += 15;
    if (password.length >= 16) strength += 10;

    this.passwordStrength = Math.min(strength, 100);

    if (this.passwordStrength <= 25) this.passwordStrengthText = 'Weak';
    else if (this.passwordStrength <= 50) this.passwordStrengthText = 'Fair';
    else if (this.passwordStrength <= 75) this.passwordStrengthText = 'Good';
    else this.passwordStrengthText = 'Strong';
  }
}
