import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModuleCreateComponent } from './module-create.component';

describe('ModuleCreateComponent', () => {
  let component: ModuleCreateComponent;
  let fixture: ComponentFixture<ModuleCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModuleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
