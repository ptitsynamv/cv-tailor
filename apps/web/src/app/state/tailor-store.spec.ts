import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { DOCUMENTS, GAPS, JOB } from '../testing/fixtures';
import { TailorStore } from './tailor-store';

describe('TailorStore', () => {
  let store: TailorStore;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(TailorStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('runs the three steps, each sending the results of the steps before it', async () => {
    const analyzing = store.analyzeJob('Senior Frontend Engineer at Acme');
    expect(store.job.loading()).toBe(true);
    const jobRequest = http.expectOne('/api/job-analysis');
    expect(jobRequest.request.body).toEqual({ jobText: 'Senior Frontend Engineer at Acme' });
    jobRequest.flush(JOB);
    await analyzing;
    expect(store.job.loading()).toBe(false);
    expect(store.job.value()).toEqual(JOB);

    const matching = store.findGaps();
    const gapRequest = http.expectOne('/api/gap-analysis');
    expect(gapRequest.request.body).toEqual(JOB);
    gapRequest.flush(GAPS);
    await matching;

    const generating = store.generateDocuments();
    const documentsRequest = http.expectOne('/api/documents');
    expect(documentsRequest.request.body).toEqual({ job: JOB, gaps: GAPS });
    documentsRequest.flush(DOCUMENTS);
    await generating;
    expect(store.documents.value()?.fileBaseName).toBe('alex-doe-acme');
  });

  it('clears the later steps when a job is analyzed again', async () => {
    store.gaps.value.set(GAPS);
    store.documents.value.set(DOCUMENTS);

    const analyzing = store.analyzeJob('Another post');

    expect(store.gaps.started()).toBe(false);
    expect(store.documents.started()).toBe(false);
    http.expectOne('/api/job-analysis').flush(JOB);
    await analyzing;
  });

  it('shows the detail of a problem response as the error', async () => {
    const analyzing = store.analyzeJob('x');
    http
      .expectOne('/api/job-analysis')
      .flush(
        { status: 502, detail: "Claude stopped with max_tokens at step 'job-analysis'" },
        { status: 502, statusText: 'Bad Gateway' },
      );
    await analyzing;

    expect(store.job.error()).toBe("Claude stopped with max_tokens at step 'job-analysis'");
    expect(store.job.value()).toBeUndefined();
    expect(store.job.loading()).toBe(false);
  });

  it('says the backend is down when the request never reaches it', async () => {
    const analyzing = store.analyzeJob('x');
    http.expectOne('/api/job-analysis').error(new ProgressEvent('error'), { status: 0 });
    await analyzing;

    expect(store.job.error()).toContain('Is the backend running on port 8080?');
  });

  it('does nothing when a step runs before the step it needs', async () => {
    await store.findGaps();
    await store.generateDocuments();

    http.expectNone('/api/gap-analysis');
    http.expectNone('/api/documents');
  });
});
