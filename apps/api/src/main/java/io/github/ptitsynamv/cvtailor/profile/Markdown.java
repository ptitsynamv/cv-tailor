package io.github.ptitsynamv.cvtailor.profile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.ptitsynamv.cvtailor.common.Slug;

/** Just enough Markdown parsing for the career-ops files: headings, bullets and pipe tables. */
final class Markdown {

	private static final Pattern SEPARATOR_ROW = Pattern.compile("^\\|[\\s:|-]+\\|$");

	private Markdown() {
	}

	record Table(List<String> headers, List<List<String>> rows) {
	}

	/** Splits lines into sections keyed by heading text (without the {@code ## } marker), in file order. */
	static Map<String, List<String>> sections(List<String> lines) {
		Map<String, List<String>> sections = new LinkedHashMap<>();
		List<String> current = null;
		for (String line : lines) {
			if (line.startsWith("## ")) {
				current = new ArrayList<>();
				sections.put(line.substring(3).strip(), current);
			}
			else if (current != null) {
				current.add(line);
			}
		}
		return sections;
	}

	/** Lines before the first {@code ## } heading. */
	static List<String> preamble(List<String> lines) {
		return lines.stream().takeWhile(line -> !line.startsWith("## ")).toList();
	}

	/** Bullet texts ({@code - item}, any indent), with emphasis removed. */
	static List<String> bullets(List<String> lines) {
		return lines.stream()
				.map(String::strip)
				.filter(line -> line.startsWith("- "))
				.map(line -> clean(line.substring(2)))
				.filter(line -> !line.isEmpty())
				.toList();
	}

	static List<Table> tables(List<String> lines) {
		List<Table> tables = new ArrayList<>();
		List<String> block = new ArrayList<>();
		for (String line : lines) {
			String stripped = line.strip();
			if (stripped.startsWith("|")) {
				block.add(stripped);
			}
			else if (!block.isEmpty()) {
				tables.add(toTable(block));
				block = new ArrayList<>();
			}
		}
		if (!block.isEmpty()) {
			tables.add(toTable(block));
		}
		return tables;
	}

	/** Removes Markdown emphasis and code markers: {@code **Role:** `Angular`} becomes {@code Role: Angular}. */
	static String clean(String text) {
		return text.replace("**", "").replace("`", "").strip();
	}

	static String slug(String text) {
		return Slug.of(clean(text));
	}

	private static Table toTable(List<String> block) {
		List<String> headers = cells(block.getFirst());
		List<List<String>> rows = block.stream()
				.skip(1)
				.filter(row -> !SEPARATOR_ROW.matcher(row).matches())
				.map(Markdown::cells)
				.toList();
		return new Table(headers, rows);
	}

	private static List<String> cells(String row) {
		String inner = row.substring(1, row.endsWith("|") ? row.length() - 1 : row.length());
		return List.of(inner.split("\\|", -1)).stream().map(Markdown::clean).toList();
	}

}
