import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';

import { Contact, DocumentsResponse } from '../api/models';
import { downloadPdf } from '../shared/download-pdf';
import { FocusOnRender } from '../shared/focus-on-render';
import { TailorStore } from '../state/tailor-store';

/** Same limit the backend checks. */
export const MAX_LETTER_WORDS = 250;

/** Step 3: preview of the CV and cover letter, and the two PDF downloads. */
@Component({
  selector: 'app-documents-step',
  imports: [FocusOnRender],
  templateUrl: './documents-step.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentsStep {
  protected readonly store = inject(TailorStore);
  /** Evidence ids next to each claim show where it comes from. They're never in the PDF. */
  protected readonly showEvidence = signal(true);
  protected readonly maxLetterWords = MAX_LETTER_WORDS;

  protected readonly letterWords = computed(
    () =>
      (this.store.documents.value()?.coverLetter.paragraphs ?? [])
        .flatMap((paragraph) => paragraph.trim().split(/\s+/))
        .filter(Boolean).length,
  );

  protected contactLine(contact: Contact): string {
    return [contact.location, contact.email, contact.phone, ...contact.links]
      .filter((detail) => detail.trim())
      .join('  ·  ');
  }

  protected toggleEvidence(event: Event): void {
    this.showEvidence.set((event.target as HTMLInputElement).checked);
  }

  protected downloadCv(documents: DocumentsResponse): void {
    downloadPdf(documents.cvPdf, `${documents.fileBaseName}-cv.pdf`);
  }

  protected downloadLetter(documents: DocumentsResponse): void {
    downloadPdf(documents.coverLetterPdf, `${documents.fileBaseName}-cover-letter.pdf`);
  }
}
