package io.github.ptitsynamv.cvtailor.profile;

public enum ProfileSection {

	EXPERIENCE("Experience"),
	PROJECT("Projects"),
	SKILL("Skills"),
	CERTIFICATE("Certificates"),
	EDUCATION("Education");

	private final String heading;

	ProfileSection(String heading) {
		this.heading = heading;
	}

	public String heading() {
		return heading;
	}

}
