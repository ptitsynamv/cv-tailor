package io.github.ptitsynamv.cvtailor.documents;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Saves generated PDFs to {@code OUTPUT_DIR/<folder>/} when OUTPUT_DIR is set (e.g. ../career-ops/applications). */
@Service
public class DocumentStorage {

	private static final Logger log = LoggerFactory.getLogger(DocumentStorage.class);

	private final DocumentsProperties properties;

	public DocumentStorage(DocumentsProperties properties) {
		this.properties = properties;
	}

	/**
	 * @param folderName a slug, so it can't escape the output folder
	 * @param files      file name to content; existing files with the same name are replaced
	 * @return the folder written to, or empty when saving is off
	 */
	public Optional<Path> save(String folderName, Map<String, byte[]> files) {
		if (!StringUtils.hasText(properties.outputDir())) {
			return Optional.empty();
		}
		Path folder = Path.of(properties.outputDir()).resolve(folderName).toAbsolutePath().normalize();
		try {
			Files.createDirectories(folder);
			for (Map.Entry<String, byte[]> file : files.entrySet()) {
				Files.write(folder.resolve(file.getKey()), file.getValue());
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("Can't save documents to " + folder, e);
		}
		log.info("Saved {} to {}", files.keySet(), folder);
		return Optional.of(folder);
	}

}
