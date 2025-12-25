import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestEditor } from './quest-editor';

describe('QuestEditor', () => {
  let component: QuestEditor;
  let fixture: ComponentFixture<QuestEditor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestEditor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestEditor);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
