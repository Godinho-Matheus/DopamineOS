import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { QuestEditorComponent } from './quest-editor';

describe('QuestEditorComponent', () => {
  let component: QuestEditorComponent;
  let fixture: ComponentFixture<QuestEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestEditorComponent],
      providers: [provideHttpClient(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestEditorComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
