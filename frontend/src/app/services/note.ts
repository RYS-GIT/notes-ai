import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Note } from '../models/note.model';
import { QuizQuestion } from '../models/quiz.model';
import { ChatResponse, ChatTurn } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private http = inject(HttpClient);
  private baseUrl = '/api/notes';

  getAllNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(this.baseUrl);
  }

  getNoteById(id: number): Observable<Note> {
    return this.http.get<Note>(`${this.baseUrl}/${id}`);
  }

  createNote(note: Note): Observable<Note> {
    return this.http.post<Note>(this.baseUrl, note);
  }

  updateNote(id: number, note: Note): Observable<Note> {
    return this.http.put<Note>(`${this.baseUrl}/${id}`, note);
  }

  deleteNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  summarizeNote(id: number): Observable<{ summary: string }> {
    return this.http.post<{ summary: string }>(`${this.baseUrl}/${id}/summarize`, {});
  }

  generateQuiz(id: number, count = 5): Observable<QuizQuestion[]> {
    return this.http.post<QuizQuestion[]>(`${this.baseUrl}/${id}/quiz`, {}, { params: { count } });
  }

  askChat(question: string, history: ChatTurn[]): Observable<ChatResponse> {
    return this.http.post<ChatResponse>('/api/chat', { question, history });
  }
}