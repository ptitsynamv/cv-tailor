package io.github.ptitsynamv.cvtailor.profile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Reads a profile folder in the career-ops layout and turns it into citable {@link ProfileItem}s.
 *
 * <p>Ids: {@code exp-<folder>} for a role and {@code exp-<folder>-<n>} for each of its bullets,
 * {@code proj-<slug>}, {@code skill-<slug>}, {@code cert-<slug>} and {@code edu-<slug>} for table rows.
 */
@Service
public class ProfileLoader {

	private static final Logger log = LoggerFactory.getLogger(ProfileLoader.class);

	/** "3. High-Impact Achievements (Metric-Driven / STAR Method)" becomes "High-Impact Achievements". */
	private static final Pattern HEADING_DECORATION = Pattern.compile("^\\d+\\.\\s*|\\s*\\(.*\\)$");

	private final Path profileDir;

	public ProfileLoader(ProfileProperties properties) {
		this.profileDir = properties.dir().toAbsolutePath().normalize();
	}

	/** Reads the files on every call, so profile edits show up without a restart. */
	public Profile load() {
		if (!Files.isDirectory(profileDir)) {
			throw new IllegalStateException("Profile folder not found: " + profileDir + " (set PROFILE_DIR)");
		}
		Set<String> usedIds = new HashSet<>();
		List<ProfileItem> items = new ArrayList<>();
		for (Path experienceFile : experienceFiles()) {
			items.addAll(experience(experienceFile, usedIds));
		}
		items.addAll(firstTableRows("projects/registry.md", ProfileSection.PROJECT, "proj", usedIds));
		items.addAll(skills(usedIds));
		items.addAll(firstTableRows("certificates/data.md", ProfileSection.CERTIFICATE, "cert", usedIds));
		items.addAll(firstTableRows("education/data.md", ProfileSection.EDUCATION, "edu", usedIds));

		Profile profile = new Profile(items, doNotClaim());
		log.info("Loaded profile from {}: {} items", profileDir, items.size());
		return profile;
	}

	private List<Path> experienceFiles() {
		Path experienceDir = profileDir.resolve("experience");
		if (!Files.isDirectory(experienceDir)) {
			log.warn("No experience folder in {}", profileDir);
			return List.of();
		}
		try (Stream<Path> companies = Files.list(experienceDir)) {
			return companies.map(company -> company.resolve("data.md")).filter(Files::isRegularFile).sorted().toList();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private List<ProfileItem> experience(Path file, Set<String> usedIds) {
		List<String> lines = readLines(file);
		String baseId = uniqueId("exp-" + Markdown.slug(file.getParent().getFileName().toString()), usedIds);

		String company = lines.stream()
				.filter(line -> line.startsWith("# "))
				.findFirst()
				.map(line -> Markdown.clean(line.substring(2)))
				.orElse("Company: " + file.getParent().getFileName());
		List<String> facts = new ArrayList<>(List.of(company));
		facts.addAll(Markdown.bullets(Markdown.preamble(lines)));

		List<ProfileItem> items = new ArrayList<>();
		items.add(new ProfileItem(baseId, ProfileSection.EXPERIENCE, String.join(" | ", facts)));
		for (Map.Entry<String, List<String>> section : Markdown.sections(lines).entrySet()) {
			String label = HEADING_DECORATION.matcher(section.getKey()).replaceAll("");
			for (String bullet : Markdown.bullets(section.getValue())) {
				String id = uniqueId(baseId + "-" + items.size(), usedIds);
				items.add(new ProfileItem(id, ProfileSection.EXPERIENCE, label + ": " + bullet));
			}
		}
		return items;
	}

	private List<ProfileItem> skills(Set<String> usedIds) {
		List<ProfileItem> items = new ArrayList<>();
		for (Map.Entry<String, List<String>> section : skillSections().entrySet()) {
			if (isNotEvidenced(section.getKey())) {
				continue;
			}
			for (Markdown.Table table : Markdown.tables(section.getValue())) {
				items.addAll(rows(table, ProfileSection.SKILL, "skill", usedIds));
			}
		}
		return items;
	}

	private List<String> doNotClaim() {
		return skillSections().entrySet().stream()
				.filter(section -> isNotEvidenced(section.getKey()))
				.flatMap(section -> Markdown.bullets(section.getValue()).stream())
				.toList();
	}

	private Map<String, List<String>> skillSections() {
		return Markdown.sections(readLines(profileDir.resolve("profile/skills-matrix.md")));
	}

	private List<ProfileItem> firstTableRows(String relativePath, ProfileSection section, String prefix,
			Set<String> usedIds) {
		return Markdown.tables(readLines(profileDir.resolve(relativePath))).stream()
				.findFirst()
				.map(table -> rows(table, section, prefix, usedIds))
				.orElseGet(List::of);
	}

	/** One item per row: the first cell names the id, all cells become "Header: value | Header: value". */
	private static List<ProfileItem> rows(Markdown.Table table, ProfileSection section, String prefix,
			Set<String> usedIds) {
		List<ProfileItem> items = new ArrayList<>();
		for (List<String> row : table.rows()) {
			if (row.isEmpty() || row.getFirst().isBlank()) {
				continue;
			}
			String text = IntStream.range(0, Math.min(row.size(), table.headers().size()))
					.filter(i -> !row.get(i).isBlank())
					.mapToObj(i -> table.headers().get(i) + ": " + row.get(i))
					.collect(Collectors.joining(" | "));
			String id = uniqueId(prefix + "-" + Markdown.slug(row.getFirst()), usedIds);
			items.add(new ProfileItem(id, section, text));
		}
		return items;
	}

	private static boolean isNotEvidenced(String heading) {
		return heading.toLowerCase().contains("not evidenced");
	}

	/** Adds a numeric suffix when two rows slug to the same id, so every id stays unique. */
	private static String uniqueId(String candidate, Set<String> usedIds) {
		String id = candidate;
		for (int n = 2; !usedIds.add(id); n++) {
			id = candidate + "-" + n;
		}
		return id;
	}

	private List<String> readLines(Path file) {
		if (!Files.isRegularFile(file)) {
			log.warn("Profile file missing, skipped: {}", file);
			return List.of();
		}
		try {
			return Files.readAllLines(file, StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Can't read " + file, e);
		}
	}

}
