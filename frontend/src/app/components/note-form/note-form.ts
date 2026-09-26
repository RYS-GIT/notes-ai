import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NoteService } from '../../services/note';
import { Note } from '../../models/note.model';

@Component({
  selector: 'app-note-form',
  imports: [FormsModule],
  templateUrl: './note-form.html',
  styleUrl: './note-form.scss'
})
export class NoteForm implements OnInit {
  private noteService = inject(NoteService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  noteId = signal<number | null>(null);
  title = signal('');
  content = signal('');
  isEditMode = signal(false);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.noteId.set(id);
      this.isEditMode.set(true);
      this.noteService.getNoteById(id).subscribe({
        next: (note) => {
          this.title.set(note.title);
          this.content.set(note.content);
        },
        error: (err) => console.error('Failed to load note', err)
      });
    }
  }

  save(): void {
    const note: Note = { title: this.title(), content: this.content() };

    if (this.isEditMode() && this.noteId() !== null) {
      this.noteService.updateNote(this.noteId()!, note).subscribe({
        next: () => this.router.navigate(['/notes']),
        error: (err) => console.error('Failed to update note', err)
      });
    } else {
      this.noteService.createNote(note).subscribe({
        next: () => this.router.navigate(['/notes']),
        error: (err) => console.error('Failed to create note', err)
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/notes']);
  }
}