package io.github.ptitsynamv.cvtailor.documents;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param outputDir where PDFs are saved, one folder per application; blank means download only. Relative paths
 *                  resolve from the working directory (apps/api with bootRun).
 */
@ConfigurationProperties("app.documents")
public record DocumentsProperties(@Nullable String outputDir) {
}
