import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { HealthResponse } from './api/models';
import { TailorStore } from './state/tailor-store';
import { DocumentsStep } from './steps/documents-step';
import { GapsStep } from './steps/gaps-step';
import { JobStep } from './steps/job-step';

/** One page, three steps. Each later step appears once the step before it has been run. */
@Component({
  selector: 'app-root',
  imports: [JobStep, GapsStep, DocumentsStep],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly store = inject(TailorStore);
  protected readonly health = httpResource<HealthResponse>(() => '/api/health');
}
