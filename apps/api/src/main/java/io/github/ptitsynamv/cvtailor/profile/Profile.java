package io.github.ptitsynamv.cvtailor.profile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The candidate profile as citable items.
 *
 * @param items      evidence, in a stable order so the rendered text (and its prompt cache) doesn't change between runs
 * @param doNotClaim skills the profile explicitly lacks; rendered without ids, so they can never be cited
 * @param contact    contact details; not part of {@link #render()}
 */
public record Profile(List<ProfileItem> items, List<String> doNotClaim, Contact contact) {

	public Profile {
		items = List.copyOf(items);
		doNotClaim = List.copyOf(doNotClaim);
	}

	public Set<String> ids() {
		return items.stream().map(ProfileItem::id).collect(Collectors.toUnmodifiableSet());
	}

	public boolean hasItem(String id) {
		return items.stream().anyMatch(item -> item.id().equals(id));
	}

	public Optional<ProfileItem> item(String id) {
		return items.stream().filter(item -> item.id().equals(id)).findFirst();
	}

	public List<ProfileItem> items(ProfileSection section) {
		return items.stream().filter(item -> item.section() == section).toList();
	}

	/** Renders the profile as the text block Claude reads, one {@code [id] text} line per item. */
	public String render() {
		var text = new StringBuilder("""
				# Candidate profile
				Each evidence line starts with its id in [brackets].
				""");
		for (ProfileSection section : ProfileSection.values()) {
			List<ProfileItem> sectionItems = items(section);
			if (sectionItems.isEmpty()) {
				continue;
			}
			text.append("\n## ").append(section.heading()).append('\n');
			sectionItems.forEach(item -> text.append('[').append(item.id()).append("] ").append(item.text()).append('\n'));
		}
		if (!doNotClaim.isEmpty()) {
			text.append("\n## Not evidenced (never claim these)\n");
			doNotClaim.forEach(line -> text.append("- ").append(line).append('\n'));
		}
		return text.toString();
	}

}
