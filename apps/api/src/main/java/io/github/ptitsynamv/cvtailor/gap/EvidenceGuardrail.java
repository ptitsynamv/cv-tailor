package io.github.ptitsynamv.cvtailor.gap;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The no-invention guardrail: Claude's citations are checked against the loaded profile in code, never trusted.
 *
 * <ul>
 * <li>A match citing any id that isn't in the profile becomes a GAP, with no evidence: one invented citation makes
 * the whole match untrustworthy.</li>
 * <li>A STRONG or PARTIAL match citing nothing becomes a GAP.</li>
 * <li>A GAP keeps only the ids that exist.</li>
 * </ul>
 */
final class EvidenceGuardrail {

	private static final Logger log = LoggerFactory.getLogger(EvidenceGuardrail.class);

	private EvidenceGuardrail() {
	}

	static GapReport verify(GapReport report, Set<String> profileIds) {
		return new GapReport(report.items().stream().map(item -> verify(item, profileIds)).toList());
	}

	private static GapItem verify(GapItem item, Set<String> profileIds) {
		List<String> unknownIds = item.evidenceIds().stream().filter(id -> !profileIds.contains(id)).toList();
		List<String> knownIds = item.evidenceIds().stream().filter(profileIds::contains).toList();

		if (item.status() == GapStatus.GAP) {
			if (!unknownIds.isEmpty()) {
				log.warn("Guardrail: dropped unknown evidence ids {} from GAP '{}'", unknownIds, item.requirement());
			}
			return new GapItem(item.requirement(), GapStatus.GAP, knownIds, item.note());
		}
		if (!unknownIds.isEmpty()) {
			log.warn("Guardrail: '{}' was {} but cited ids not in the profile {}; marked GAP", item.requirement(),
					item.status(), unknownIds);
			return new GapItem(item.requirement(), GapStatus.GAP, List.of(),
					"Guardrail: marked GAP because the match cited evidence that isn't in the profile (%s)."
							.formatted(String.join(", ", unknownIds)));
		}
		if (knownIds.isEmpty()) {
			log.warn("Guardrail: '{}' was {} without evidence; marked GAP", item.requirement(), item.status());
			return new GapItem(item.requirement(), GapStatus.GAP, List.of(),
					"Guardrail: marked GAP because the match cited no evidence.");
		}
		return item;
	}

}
