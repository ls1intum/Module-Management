import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProposalViewComponent } from './proposal-view.component';

describe('ProposalViewComponent', () => {
  let component: ProposalViewComponent;
  let fixture: ComponentFixture<ProposalViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProposalViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProposalViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
