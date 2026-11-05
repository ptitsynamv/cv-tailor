/** Saves a Base64-encoded PDF (as sent by `POST /api/documents`) under `fileName`. */
export function downloadPdf(base64: string, fileName: string): void {
  const bytes = Uint8Array.from(atob(base64), (char) => char.charCodeAt(0));
  const url = URL.createObjectURL(new Blob([bytes], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  // Revoke after the click has started the download.
  setTimeout(() => URL.revokeObjectURL(url));
}
