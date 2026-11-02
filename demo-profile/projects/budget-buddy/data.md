# Project: Budget Buddy
- **Category:** Frontend Architecture
- **Status:** Completed
- **Period:** Feb 2025 – May 2025
- **Repo URL:** https://github.com/alex-doe-demo/budget-buddy (fictional)
- **Live Demo:** N/A
- **Role:** Creator & Sole Maintainer

## 1. One-Sentence Elevator Pitch (For Resume Bullet / Header)
- Zoneless Angular 20 budgeting app with signal-based state, offline storage in IndexedDB and an installable PWA.

## 2. Problem Statement & Motivation
- Built to try a fully zoneless, Signals-first Angular architecture end to end before using it at work.

## 3. Tech Stack Matrix
- **Core Framework/Language:** Angular 20 (standalone, zoneless), TypeScript
- **State & Data Fetching:** Angular Signals, `computed`, `linkedSignal`
- **Styling & UI:** Tailwind CSS, Angular CDK
- **Backend / Cloud / APIs:** None (IndexedDB for local storage)
- **DevOps & Tooling:** Angular CLI, Vitest, PWA service worker

## 4. Key Architectural & Technical Highlights
- **Signals-first state:** a small store built only from `signal` and `computed`, without NgRx.
- **Offline first:** budgets and transactions saved in IndexedDB and synced on start-up.
- **Installable:** PWA manifest and service worker for offline use.

## 5. Ready-to-Use CV Bullets (STAR Format)
- Architected a zoneless Angular 20 app with a signal-based store, with no zone.js and no external state library.
- Implemented offline storage with IndexedDB and a PWA service worker, so the app works without a connection.
- Built the UI with Tailwind CSS and Angular CDK, and covered the store with Vitest unit tests.

## 6. Relevance Tags (For Job Description Matching)
`Angular`, `Angular 20`, `Signals`, `Zoneless`, `Standalone Components`, `Tailwind CSS`, `Angular CDK`, `PWA`, `IndexedDB`, `Vitest`, `TypeScript`
