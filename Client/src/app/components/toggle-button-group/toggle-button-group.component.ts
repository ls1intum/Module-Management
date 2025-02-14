import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface ToggleOption {
  value: any;
  label: string;
}

@Component({
  selector: 'app-toggle-button-group',
  standalone: true,
  imports: [CommonModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: ToggleButtonGroupComponent,
      multi: true,
    },
  ],
  templateUrl: './toggle-button-group.component.html',
  styleUrls: ['./toggle-button-group.component.css']
})
export class ToggleButtonGroupComponent implements ControlValueAccessor {
  @Input() options: ToggleOption[] = [];
  @Input() required = false;
  @Output() valueChange = new EventEmitter<any>();

  selectedValue: any = null;
  disabled = false;
  onChange: any = () => {};
  onTouched: any = () => {};

  writeValue(value: any): void {
    this.selectedValue = value;
    if (this.required && !value && this.options.length > 0) {
      this.selectedValue = this.options[0].value;
      this.onChange(this.selectedValue);
      this.valueChange.emit(this.selectedValue);
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onToggleClick(value: any): void {
    if (this.disabled) return;
    
    if (this.required) {
      this.selectedValue = value;
    } else {
      this.selectedValue = this.selectedValue === value ? null : value;
    }

    this.onChange(this.selectedValue);
    this.onTouched();
    this.valueChange.emit(this.selectedValue);
  }
}