import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly storageKey = 'notesai_auth';

  isLoggedIn = signal(!!sessionStorage.getItem(this.storageKey));

  login(username: string, password: string): Observable<unknown> {
    const encoded = btoa(`${username}:${password}`);
    // Verify the credentials actually work by hitting a real endpoint
    return this.http
      .get('/api/notes', {
        headers: { Authorization: `Basic ${encoded}` }
      })
      .pipe(
        tap(() => {
          sessionStorage.setItem(this.storageKey, encoded);
          this.isLoggedIn.set(true);
        })
      );
  }

  logout(): void {
    sessionStorage.removeItem(this.storageKey);
    this.isLoggedIn.set(false);
  }

  getAuthHeader(): string | null {
    const encoded = sessionStorage.getItem(this.storageKey);
    return encoded ? `Basic ${encoded}` : null;
  }
}