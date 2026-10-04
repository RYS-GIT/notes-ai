import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NoteService } from '../../services/note';
import { ChatMessage } from '../../models/chat.model';

@Component({
  selector: 'app-chat',
  imports: [FormsModule],
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

    this.noteService.askChat(q).subscribe({
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