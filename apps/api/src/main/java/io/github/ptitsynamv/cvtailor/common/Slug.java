package io.github.ptitsynamv.cvtailor.common;

import java.util.regex.Pattern;

public final class Slug {

	private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");

	private Slug() {
	}

	/** {@code "Bachelor’s Degree"} becomes {@code "bachelors-degree"}. */
	public static String of(String text) {
		String noApostrophes = text.toLowerCase().replace("’", "").replace("'", "");
		return NON_SLUG_CHARS.matcher(noApostrophes).replaceAll("-").replaceAll("^-+|-+$", "");
	}

}
