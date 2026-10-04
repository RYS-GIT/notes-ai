import { Routes } from '@angular/router';
import { NoteList } from './components/note-list/note-list';
import { NoteForm } from './components/note-form/note-form';
import { Chat } from './components/chat/chat';

export const routes: Routes = [
  { path: '', redirectTo: 'notes', pathMatch: 'full' },
  { path: 'notes', component: NoteList },
  { path: 'notes/new', component: NoteForm },
  { path: 'notes/:id/edit', component: NoteForm },
  { path: 'chat', component: Chat }
];