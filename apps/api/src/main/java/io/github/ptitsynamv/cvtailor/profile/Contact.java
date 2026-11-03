package io.github.ptitsynamv.cvtailor.profile;

import java.util.List;
import java.util.stream.Stream;

/**
 * Contact details from {@code profile/contacts.yaml}. Never rendered into the text Claude reads; code adds them to
 * the documents.
 *
 * @param links display-ready links, e.g. {@code linkedin.com/in/alex-doe-demo}
 */
public record Contact(String name, String headline, String location, String email, String phone, List<String> links) {

	public static final Contact EMPTY = new Contact("", "", "", "", "", List.of());

	public Contact {
		links = List.copyOf(links);
	}

	/** Location, email, phone and links, skipping empty ones: the contact line of a CV or letter. */
	public List<String> details() {
		return Stream.concat(Stream.of(location, email, phone), links.stream()).filter(detail -> !detail.isBlank()).toList();
	}

}
