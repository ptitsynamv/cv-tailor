package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentStorageTest {

	@TempDir
	private Path outputDir;

	@Test
	void savesTheFilesInAFolderPerApplication() throws IOException {
		var storage = new DocumentStorage(new DocumentsProperties(outputDir.toString()));

		Path folder = storage.save("2026-11-03-acme-frontend-engineer", Map.of("cv.pdf", new byte[] { 1, 2 })).orElseThrow();

		assertThat(folder).isEqualTo(outputDir.resolve("2026-11-03-acme-frontend-engineer"));
		assertThat(Files.readAllBytes(folder.resolve("cv.pdf"))).containsExactly(1, 2);
	}

	@Test
	void savesNothingWhenNoOutputFolderIsSet() {
		var storage = new DocumentStorage(new DocumentsProperties(""));

		assertThat(storage.save("folder", Map.of("cv.pdf", new byte[] { 1 }))).isEmpty();
	}

}
