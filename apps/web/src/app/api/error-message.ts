import { HttpErrorResponse } from '@angular/common/http';

const UNREACHABLE = "Can't reach the API. Is the backend running on port 8080?";

/**
 * A message for the user. The backend sends problem JSON whose `detail` is already written for people
 * (validation messages, failed Claude calls); anything else means the backend didn't answer.
 */
export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Something went wrong. Please try again.';
  }
  const detail = (error.error as { detail?: unknown } | null)?.detail;
  if (typeof detail === 'string' && detail.trim()) {
    return detail;
  }
  return error.status === 0 || error.status >= 500
    ? UNREACHABLE
    : `The request failed (HTTP ${error.status}).`;
}
