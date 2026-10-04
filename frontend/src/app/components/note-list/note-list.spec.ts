import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoteList } from './note-list';

describe('NoteList', () => {
  let component: NoteList;
  let fixture: ComponentFixture<NoteList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteList],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(NoteList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});