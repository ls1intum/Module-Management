import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { LayoutComponent } from './components/layout.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, LayoutComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'CIT Module Management';
}
