import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NoteService } from '../../services/note';
import { Note } from '../../models/note.model';
import { QuizQuestion } from '../../models/quiz.model';

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
  saving = signal(false);
  saveError = signal<string | null>(null);

  // AI state
  summary = signal<string | null>(null);
  quiz = signal<QuizQuestion[]>([]);
  answers = signal<Record<number, number>>({});
  summaryLoading = signal(false);
  quizLoading = signal(false);
  aiError = signal<string | null>(null);

  score = computed(() =>
    this.quiz().reduce((total, q, i) => total + (this.answers()[i] === q.correctIndex ? 1 : 0), 0)
  );
  allAnswered = computed(
    () => this.quiz().length > 0 && Object.keys(this.answers()).length === this.quiz().length
  );

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
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);

    const note: Note = { title: this.title(), content: this.content() };
    const request$ =
      this.isEditMode() && this.noteId() !== null
        ? this.noteService.updateNote(this.noteId()!, note)
        : this.noteService.createNote(note);

    request$.subscribe({
      next: () => this.router.navigate(['/notes']),
      error: (err) => {
        this.saving.set(false);
        this.saveError.set(err.error?.message ?? 'Could not save the note. Please try again.');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/notes']);
  }

  summarize(): void {
    const id = this.noteId();
    if (id === null) return;

    this.aiError.set(null);
    this.summaryLoading.set(true);
    this.noteService.summarizeNote(id).subscribe({
      next: (res) => {
        this.summary.set(res.summary);
        this.summaryLoading.set(false);
      },
      error: (err) => {
        this.aiError.set(err.error?.message ?? 'Could not reach the server. Please try again.');
        this.summaryLoading.set(false);
      }
    });
  }

  generateQuiz(): void {
    const id = this.noteId();
    if (id === null) return;

    this.aiError.set(null);
    this.quizLoading.set(true);
    this.noteService.generateQuiz(id, 5).subscribe({
      next: (questions) => {
        this.quiz.set(questions);
        this.answers.set({});
        this.quizLoading.set(false);
      },
      error: (err) => {
        this.aiError.set(err.error?.message ?? 'Could not reach the server. Please try again.');
        this.quizLoading.set(false);
      }
    });
  }

  pick(questionIndex: number, optionIndex: number): void {
    if (this.answers()[questionIndex] !== undefined) return; // already answered
    this.answers.update((a) => ({ ...a, [questionIndex]: optionIndex }));
  }

  optionClass(questionIndex: number, optionIndex: number): string {
    const picked = this.answers()[questionIndex];
    if (picked === undefined) return '';
    if (optionIndex === this.quiz()[questionIndex].correctIndex) return 'correct';
    if (optionIndex === picked) return 'wrong';
    return '';
  }
}