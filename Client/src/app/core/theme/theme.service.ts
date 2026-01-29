import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly DARK_MODE_CLASS = 'dark';
  private readonly THEME_STORAGE_KEY = 'theme-preference-is-dark';

  // Signal to track current theme
  isDarkMode = signal<boolean>(this.getInitialTheme());

  constructor() {
    this.applyTheme(this.isDarkMode());
  }

  private getInitialTheme(): boolean {
    // Check localStorage first
    const stored = localStorage.getItem(this.THEME_STORAGE_KEY);
    if (stored) {
      return stored === 'true';
    }

    // Fall back to system preference
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    return false;
  }

  toggleTheme(): void {
    const isDark = !this.isDarkMode();
    this.isDarkMode.set(isDark);
    this.applyTheme(isDark);
    localStorage.setItem(this.THEME_STORAGE_KEY, isDark.toString());
  }

  private applyTheme(isDark: boolean): void {
    const element = document.querySelector('html');
    if (isDark) {
      element?.classList.add(this.DARK_MODE_CLASS);
    } else {
      element?.classList.remove(this.DARK_MODE_CLASS);
    }
  }
}
