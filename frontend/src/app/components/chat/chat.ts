import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NoteService } from '../../services/note';
import { ChatMessage, ChatTurn } from '../../models/chat.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-chat',
  imports: [FormsModule, RouterLink],
  templateUrl: './chat.html',
  styleUrl: './chat.scss'
})
export class Chat {
  private noteService = inject(NoteService);

  messages = signal<ChatMessage[]>([]);
  question = signal('');
  loading = signal(false);

  ask(): void {
    const q = this.question().trim();
    if (!q || this.loading()) return;

    this.messages.update((m) => [...m, { role: 'user', text: q }]);
    this.question.set('');
    this.loading.set(true);

    const history: ChatTurn[] = this.messages().map((m) => ({ role: m.role, text: m.text }));

    this.noteService.askChat(q, history).subscribe({
      next: (res) => {
        this.messages.update((m) => [
          ...m,
          { role: 'assistant', text: res.answer, sourceNoteIds: res.sourceNoteIds }
        ]);
        this.loading.set(false);
      },
      error: (err) => {
        const message = err.error?.message ?? 'Something went wrong. Please try again.';
        this.messages.update((m) => [...m, { role: 'assistant', text: message }]);
        this.loading.set(false);
      }
    });
  }
}