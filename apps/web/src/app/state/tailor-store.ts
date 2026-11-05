import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { DocumentsResponse, GapReport, JobAnalysis } from '../api/models';
import { AsyncStep } from './async-step';

/**
 * State of the three steps. Each step feeds the next, so running a step again clears the steps after it:
 * gaps and documents always belong to the job analysis on screen.
 */
@Injectable({ providedIn: 'root' })
export class TailorStore {
  private readonly http = inject(HttpClient);

  readonly job = new AsyncStep<JobAnalysis>();
  readonly gaps = new AsyncStep<GapReport>();
  readonly documents = new AsyncStep<DocumentsResponse>();

  async analyzeJob(jobText: string): Promise<void> {
    this.gaps.reset();
    this.documents.reset();
    await this.job.run(() => this.http.post<JobAnalysis>('/api/job-analysis', { jobText }));
  }

  async findGaps(): Promise<void> {
    const job = this.job.value();
    if (!job) {
      return;
    }
    this.documents.reset();
    await this.gaps.run(() => this.http.post<GapReport>('/api/gap-analysis', job));
  }

  async generateDocuments(): Promise<void> {
    const job = this.job.value();
    const gaps = this.gaps.value();
    if (!job || !gaps) {
      return;
    }
    await this.documents.run(() =>
      this.http.post<DocumentsResponse>('/api/documents', { job, gaps }),
    );
  }
}
