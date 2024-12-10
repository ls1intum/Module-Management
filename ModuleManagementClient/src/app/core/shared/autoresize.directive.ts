import { Directive, ElementRef, AfterViewInit } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: 'textarea[autoResize]'
})
export class AutoResizeDirective implements AfterViewInit {
  constructor(private el: ElementRef<HTMLTextAreaElement>, private ngControl: NgControl) {}

  ngAfterViewInit(): void {
    this.adjust();
    if (this.ngControl && this.ngControl.valueChanges) {
      this.ngControl.valueChanges.subscribe(() => {
        setTimeout(() => this.adjust());
      });
    }
  }

  private adjust() {
    const textArea = this.el.nativeElement;
    textArea.style.overflow = 'hidden';
    textArea.style.height = 'auto';
    textArea.style.height = textArea.scrollHeight + 'px';
  }
}
