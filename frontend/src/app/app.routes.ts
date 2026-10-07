import { Routes } from '@angular/router';
import { NoteList } from './components/note-list/note-list';
import { NoteForm } from './components/note-form/note-form';
import { Chat } from './components/chat/chat';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'notes', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'notes', component: NoteList, canActivate: [authGuard] },
  { path: 'notes/new', component: NoteForm, canActivate: [authGuard] },
  { path: 'notes/:id/edit', component: NoteForm, canActivate: [authGuard] },
  { path: 'chat', component: Chat, canActivate: [authGuard] }
];