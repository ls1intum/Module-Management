import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvatarModule } from 'primeng/avatar';
import { MessageModule } from 'primeng/message';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'feedback-message',
  standalone: true,
  imports: [CommonModule, AvatarModule, MessageModule, TooltipModule],
  template: `
    <div class="w-fit min-w-40" [ngClass]="styleClass()">
      <p-message severity="error" variant="outlined" [closable]="true">
        <div class="flex items-center gap-2">
          <p-avatar [label]="initials()" [pTooltip]="authorDisplay()" tooltipPosition="top" size="normal" shape="square" />
          <span>{{ feedbackText() }}</span>
        </div>
      </p-message>
    </div>
  `
})
export class FeedbackMessageComponent {
  /** Display name or label (e.g. "John Doe" or "Rejection reason"). Shown in tooltip. */
  authorDisplay = input.required<string>();
  /** Optional. When provided (user name), used to compute initials. When omitted (role/label), no initials shown. */
  authorUserName = input<string | null | undefined>(null);
  /** The feedback/rejection text content. */
  feedbackText = input.required<string>();
  /** Optional extra CSS classes for the p-message wrapper. */
  styleClass = input<string>('');

  /** Initials only from authorUserName (person's name). Role labels produce no initials. */
  initials = computed(() => {
    const words = this.authorUserName()?.trim().split(/\s+/)?.filter(Boolean) ?? [];
    if (words.length === 0) return '—';
    return words.length === 1 ? words[0].slice(0, 2).toUpperCase() : (words[0][0] + words.at(-1)![0]).toUpperCase();
  });
}
