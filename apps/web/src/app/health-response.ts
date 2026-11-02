/** Mirrors the backend `HealthResponse` record returned by `GET /api/health`. */
export interface HealthResponse {
  status: string;
  service: string;
}
