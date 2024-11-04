import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModuleVersionEditComponent } from './module-version-edit.component';

describe('ModuleVersionEditComponent', () => {
  let component: ModuleVersionEditComponent;
  let fixture: ComponentFixture<ModuleVersionEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleVersionEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModuleVersionEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
