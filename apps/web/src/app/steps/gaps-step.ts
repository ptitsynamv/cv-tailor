import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { GapStatus } from '../api/models';
import { FocusOnRender } from '../shared/focus-on-render';
import { TailorStore } from '../state/tailor-store';

const STATUS_LABELS: Record<GapStatus, string> = {
  STRONG: 'Strong',
  PARTIAL: 'Partial',
  GAP: 'Gap',
};

/** Step 2: each requirement matched to profile evidence, then "Generate documents". */
@Component({
  selector: 'app-gaps-step',
  imports: [FocusOnRender],
  templateUrl: './gaps-step.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GapsStep {
  protected readonly store = inject(TailorStore);
  protected readonly statusLabels = STATUS_LABELS;

  protected readonly counts = computed(() => {
    const items = this.store.gaps.value()?.items ?? [];
    const count = (status: GapStatus) => items.filter((item) => item.status === status).length;
    return { strong: count('STRONG'), partial: count('PARTIAL'), gap: count('GAP') };
  });
}
