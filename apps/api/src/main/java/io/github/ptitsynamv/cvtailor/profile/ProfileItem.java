package io.github.ptitsynamv.cvtailor.profile;

import java.util.Map;

/**
 * One citable piece of evidence from the profile.
 *
 * @param id     stable id Claude cites, e.g. {@code exp-lumenledger-3} or {@code proj-budget-buddy}
 * @param text   the evidence as plain text, without Markdown emphasis
 * @param fields structured facts, keyed as in the source file: table cells by column header ("Project Name"), and
 *               for a role "Company", "Role / Title", "Period", ... Code copies these into the CV, so names and
 *               dates never come from the model. Empty for bullets.
 */
public record ProfileItem(String id, ProfileSection section, String text, Map<String, String> fields) {

	public ProfileItem {
		fields = Map.copyOf(fields);
	}

	public String field(String name) {
		return fields.getOrDefault(name, "");
	}

}
