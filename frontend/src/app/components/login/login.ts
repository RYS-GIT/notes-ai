import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);

  username = signal('');
  password = signal('');
  checking = signal(false);
  error = signal<string | null>(null);

  submit(): void {
    this.error.set(null);
    this.checking.set(true);
    this.auth.login(this.username(), this.password()).subscribe({
      next: () => {
        this.checking.set(false);
        this.router.navigate(['/notes']);
      },
      error: () => {
        this.checking.set(false);
        this.error.set('Incorrect username or password.');
      }
    });
  }
}