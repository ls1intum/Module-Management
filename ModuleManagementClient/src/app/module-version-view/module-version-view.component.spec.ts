import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModuleVersionViewComponent } from './module-version-view.component';

describe('ModuleVersionViewComponent', () => {
  let component: ModuleVersionViewComponent;
  let fixture: ComponentFixture<ModuleVersionViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleVersionViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModuleVersionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
