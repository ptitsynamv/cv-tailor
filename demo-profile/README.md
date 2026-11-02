# Demo Profile: Alex Doe (fictional)

Everything in this folder is **made up**: the person, the companies, the projects, the dates and the contact details. It exists so the app can run publicly and in tests without any real personal data.

It mirrors the folder layout of a private `career-ops` repo, so the same `ProfileLoader` reads both:

| Path | Content |
| :--- | :--- |
| `profile/contacts.yaml` | Name, contacts, preferences |
| `profile/summary.md` | Summary variants |
| `profile/skills-matrix.md` | Skills by level, with evidence |
| `profile/languages.md` | Languages |
| `experience/<Company>/data.md` | One file per employer (2) |
| `projects/registry.md` | Project index |
| `projects/<slug>/data.md` | One file per pet project (2) |
| `certificates/data.md` | Certifications (1) |
| `education/data.md` | Degree |

The profile is deliberately missing some skills (e.g. React in production, GraphQL, Kubernetes), so gap analysis has real gaps to find.
