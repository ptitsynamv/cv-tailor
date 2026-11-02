package io.github.ptitsynamv.cvtailor.profile;

/**
 * One citable piece of evidence from the profile.
 *
 * @param id   stable id Claude cites, e.g. {@code exp-lumenledger-3} or {@code proj-budget-buddy}
 * @param text the evidence as plain text, without Markdown emphasis
 */
public record ProfileItem(String id, ProfileSection section, String text) {
}
