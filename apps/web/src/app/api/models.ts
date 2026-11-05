// Mirrors of the backend records (package names in brackets). Keep them in sync by hand.

/** [health] `GET /api/health`. `FAKE`: Claude answers are canned fixtures because no API key is set. */
export interface HealthResponse {
  status: string;
  service: string;
  mode: 'FAKE' | 'LIVE';
}

export type Seniority = 'JUNIOR' | 'MIDDLE' | 'SENIOR' | 'LEAD' | 'PRINCIPAL' | 'NOT_STATED';

/** [job] `POST /api/job-analysis` answer. */
export interface JobAnalysis {
  title: string;
  company: string;
  seniority: Seniority;
  mustHave: string[];
  niceToHave: string[];
  keywords: string[];
}

export type GapStatus = 'STRONG' | 'PARTIAL' | 'GAP';

/** [gap] One requirement matched against the profile. */
export interface GapItem {
  requirement: string;
  status: GapStatus;
  evidenceIds: string[];
  note: string;
}

/** [gap] `POST /api/gap-analysis` answer. */
export interface GapReport {
  items: GapItem[];
}

/** [documents] One CV claim and the profile ids it's based on. */
export interface CvBullet {
  text: string;
  evidenceIds: string[];
}

export interface CvSkillGroup {
  name: string;
  skills: string[];
  evidenceIds: string[];
}

export interface Contact {
  name: string;
  headline: string;
  location: string;
  email: string;
  phone: string;
  links: string[];
}

export interface CvRole {
  id: string;
  title: string;
  company: string;
  period: string;
  location: string;
  bullets: CvBullet[];
}

export interface CvProject {
  id: string;
  name: string;
  stack: string;
  bullets: CvBullet[];
}

export interface TailoredCv {
  contact: Contact;
  summary: CvBullet[];
  skills: CvSkillGroup[];
  experience: CvRole[];
  projects: CvProject[];
  certificates: string[];
  education: string[];
}

export interface CoverLetter {
  greeting: string;
  paragraphs: string[];
}

/** [documents] `POST /api/documents` answer. The PDFs arrive as Base64 strings. */
export interface DocumentsResponse {
  cv: TailoredCv;
  coverLetter: CoverLetter;
  fileBaseName: string;
  cvPdf: string;
  coverLetterPdf: string;
  savedTo: string | null;
}
