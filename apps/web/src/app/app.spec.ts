import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { App } from './app';
import { TailorStore } from './state/tailor-store';
import { DOCUMENTS, GAPS, JOB } from './testing/fixtures';

describe('App', () => {
  let fixture: ComponentFixture<App>;
  let http: HttpTestingController;
  let store: TailorStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(App);
    http = TestBed.inject(HttpTestingController);
    store = TestBed.inject(TailorStore);
    // Run the health resource's effect so it sends the request; whenStable() would wait on it forever.
    TestBed.tick();
  });

  /** Lets awaited requests resolve, then renders. whenStable() would wait on any pending request. */
  async function settle() {
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();
  }

  const page = () => fixture.nativeElement as HTMLElement;
  const button = (label: string) =>
    [...page().querySelectorAll('button')].find((b) => b.textContent?.trim() === label);

  async function answerHealth(mode: 'FAKE' | 'LIVE') {
    http.expectOne('/api/health').flush({ status: 'UP', service: 'cv-tailor-api', mode });
    await settle();
  }

  it('shows the demo banner when the backend runs in fake mode', async () => {
    await answerHealth('FAKE');

    expect(page().textContent).toContain('Demo mode: canned responses.');
  });

  it('hides the demo banner with a real API key', async () => {
    await answerHealth('LIVE');

    expect(page().textContent).not.toContain('Demo mode');
  });

  it('asks for the job text without calling the API when the textarea is empty', async () => {
    await answerHealth('LIVE');

    button('Analyze')!.click();
    await settle();

    expect(page().textContent).toContain('Paste the job post text');
    expect(page().querySelector('#job-text')!.getAttribute('aria-invalid')).toBe('true');
    http.expectNone('/api/job-analysis');
  });

  it('walks through the three steps and moves focus to each new heading', async () => {
    await answerHealth('FAKE');
    const textarea = page().querySelector<HTMLTextAreaElement>('#job-text')!;
    textarea.value = 'Senior Frontend Engineer at Acme';
    textarea.dispatchEvent(new Event('input'));

    button('Analyze')!.click();
    await settle();
    expect(page().querySelector('[role="status"]')?.textContent).toContain('Reading the job post');
    http.expectOne('/api/job-analysis').flush(JOB);
    await settle();
    expect(page().textContent).toContain('5+ years of Angular');
    expect(document.activeElement?.textContent).toBe('Requirements');

    button('Find gaps')!.click();
    await settle();
    expect(document.activeElement?.id).toBe('gaps-heading');
    http.expectOne('/api/gap-analysis').flush(GAPS);
    await settle();
    const rows = page().querySelectorAll('tbody tr');
    expect(rows.length).toBe(3);
    expect(rows[2].textContent).toContain('Gap');
    expect(rows[2].textContent).toContain('Guardrail:');
    expect(page().textContent).toContain('1 strong, 1 partial, 1 gap.');

    button('Generate documents')!.click();
    await settle();
    expect(document.activeElement?.id).toBe('documents-heading');
    http.expectOne('/api/documents').flush(DOCUMENTS);
    await settle();
    expect(page().textContent).toContain('Senior Frontend Engineer, Lumen Ledger');
    expect(page().textContent).toContain('[exp-lumenledger-1]');
    expect(page().textContent).toContain('7 / 250 words');
    expect(button('Download CV (PDF)')).toBeTruthy();
    expect(button('Download cover letter (PDF)')).toBeTruthy();
    expect(store.documents.value()).toEqual(DOCUMENTS);
  });

  it('shows a failed step as an alert with a retry button', async () => {
    await answerHealth('LIVE');
    store.job.value.set(JOB);
    await settle();

    button('Find gaps')!.click();
    await settle();
    http
      .expectOne('/api/gap-analysis')
      .flush(
        { detail: 'The Claude API request failed' },
        { status: 502, statusText: 'Bad Gateway' },
      );
    await settle();

    expect(page().querySelector('[role="alert"]')?.textContent).toContain(
      'The Claude API request failed',
    );
    expect(button('Try again')).toBeTruthy();
  });
});
