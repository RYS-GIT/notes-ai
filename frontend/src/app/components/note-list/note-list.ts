import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NoteService } from '../../services/note';
import { Note } from '../../models/note.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-note-list',
  imports: [RouterLink],
  templateUrl: './note-list.html',
  styleUrl: './note-list.scss'
})
export class NoteList implements OnInit {
  private noteService = inject(NoteService);
  private router = inject(Router);

  notes = signal<Note[]>([]);

  ngOnInit(): void {
    this.loadNotes();
  }

  loadNotes(): void {
    this.noteService.getAllNotes().subscribe({
      next: (notes) => this.notes.set(notes),
      error: (err) => console.error('Failed to load notes', err)
    });
  }

  deleteNote(id: number): void {
    this.noteService.deleteNote(id).subscribe({
      next: () => this.loadNotes(),
      error: (err) => console.error('Failed to delete note', err)
    });
  }

  goToEdit(id: number): void {
    this.router.navigate(['/notes', id, 'edit']);
  }

  goToCreate(): void {
    this.router.navigate(['/notes/new']);
  }
}
