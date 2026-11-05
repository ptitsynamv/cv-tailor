import { DocumentsResponse, GapReport, JobAnalysis } from '../api/models';

// Small fictional answers for specs, in the shape the backend sends.

export const JOB: JobAnalysis = {
  title: 'Senior Frontend Engineer',
  company: 'Acme',
  seniority: 'SENIOR',
  mustHave: ['5+ years of Angular', 'TypeScript'],
  niceToHave: ['GraphQL'],
  keywords: ['Angular', 'TypeScript', 'GraphQL'],
};

export const GAPS: GapReport = {
  items: [
    {
      requirement: '5+ years of Angular',
      status: 'STRONG',
      evidenceIds: ['skill-angular', 'exp-lumenledger-1'],
      note: 'Six years of Angular.',
    },
    {
      requirement: 'TypeScript',
      status: 'PARTIAL',
      evidenceIds: ['skill-typescript'],
      note: 'Used daily.',
    },
    {
      requirement: 'GraphQL',
      status: 'GAP',
      evidenceIds: [],
      note: 'Guardrail: cited proj-invented, which is not in the profile.',
    },
  ],
};

export const DOCUMENTS: DocumentsResponse = {
  cv: {
    contact: {
      name: 'Alex Doe',
      headline: 'Senior Frontend Engineer',
      location: 'Remote, EU',
      email: 'alex.doe@example.com',
      phone: '',
      links: ['github.com/alex-doe-demo'],
    },
    summary: [
      { text: 'Frontend engineer with six years of Angular.', evidenceIds: ['skill-angular'] },
    ],
    skills: [
      { name: 'Frontend', skills: ['Angular', 'TypeScript'], evidenceIds: ['skill-angular'] },
    ],
    experience: [
      {
        id: 'exp-lumenledger',
        title: 'Senior Frontend Engineer',
        company: 'Lumen Ledger',
        period: 'Apr 2022 – Present',
        location: 'Remote, EU',
        bullets: [{ text: 'Led the invoicing frontend.', evidenceIds: ['exp-lumenledger-1'] }],
      },
    ],
    projects: [],
    certificates: ['AWS Certified Cloud Practitioner, AWS'],
    education: ['B.Sc. in Computer Science'],
  },
  coverLetter: {
    greeting: 'Dear Hiring Team,',
    paragraphs: ['I would like to apply.', 'Thank you.'],
  },
  fileBaseName: 'alex-doe-acme',
  // "%PDF-" in Base64.
  cvPdf: 'JVBERi0=',
  coverLetterPdf: 'JVBERi0=',
  savedTo: null,
};
