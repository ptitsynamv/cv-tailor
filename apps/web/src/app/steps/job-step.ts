import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { Seniority } from '../api/models';
import { FocusOnRender } from '../shared/focus-on-render';
import { TailorStore } from '../state/tailor-store';

/** Same limit as the backend's `JobAnalysisRequest`. */
export const MAX_JOB_TEXT = 20_000;

const SENIORITY_LABELS: Record<Seniority, string> = {
  JUNIOR: 'Junior',
  MIDDLE: 'Middle',
  SENIOR: 'Senior',
  LEAD: 'Lead',
  PRINCIPAL: 'Principal',
  NOT_STATED: 'Not stated',
};

/** Step 1: paste a job post, then review the extracted requirements. */
@Component({
  selector: 'app-job-step',
  imports: [FocusOnRender],
  templateUrl: './job-step.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobStep {
  protected readonly store = inject(TailorStore);
  protected readonly jobText = signal('');
  /** Shown without a request when the textarea is empty. */
  protected readonly inputError = signal<string | undefined>(undefined);
  protected readonly maxLength = MAX_JOB_TEXT;
  protected readonly seniorityLabels = SENIORITY_LABELS;

  protected onInput(event: Event): void {
    this.jobText.set((event.target as HTMLTextAreaElement).value);
    this.inputError.set(undefined);
  }

  protected analyze(event: Event): void {
    event.preventDefault();
    const text = this.jobText().trim();
    if (!text) {
      this.inputError.set('Paste the job post text');
      return;
    }
    void this.store.analyzeJob(text);
  }
}
