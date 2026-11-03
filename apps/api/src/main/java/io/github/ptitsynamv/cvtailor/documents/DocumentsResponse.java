package io.github.ptitsynamv.cvtailor.documents;

import org.jspecify.annotations.Nullable;

/**
 * Both documents, as data for the preview and as PDFs (byte arrays serialize as Base64 in JSON).
 *
 * @param fileBaseName suggested download name without suffix, e.g. {@code alex-doe-wix}
 * @param savedTo      folder the PDFs were saved to, or null when OUTPUT_DIR isn't set
 */
public record DocumentsResponse(TailoredCv cv, CoverLetter coverLetter, String fileBaseName, byte[] cvPdf,
		byte[] coverLetterPdf, @Nullable String savedTo) {
}
