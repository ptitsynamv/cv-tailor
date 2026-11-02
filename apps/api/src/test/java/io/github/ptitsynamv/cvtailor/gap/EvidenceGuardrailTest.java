package io.github.ptitsynamv.cvtailor.gap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class EvidenceGuardrailTest {

	private static final Set<String> PROFILE_IDS = Set.of("exp-a-1", "skill-angular");

	@Test
	void keepsAMatchWhoseEvidenceExists() {
		var item = new GapItem("Angular", GapStatus.STRONG, List.of("skill-angular", "exp-a-1"), "6+ years.");

		assertThat(verify(item)).isEqualTo(item);
	}

	@Test
	void marksAMatchCitingAnInventedIdAsGapWithoutEvidence() {
		var item = new GapItem("AI tools", GapStatus.STRONG, List.of("exp-a-1", "proj-invented"), "Built an AI tool.");

		GapItem verified = verify(item);

		assertThat(verified.status()).isEqualTo(GapStatus.GAP);
		assertThat(verified.evidenceIds()).isEmpty();
		assertThat(verified.note()).startsWith("Guardrail:").contains("proj-invented");
		assertThat(verified.requirement()).isEqualTo("AI tools");
	}

	@Test
	void marksAPartialMatchWithoutEvidenceAsGap() {
		var item = new GapItem("Node.js", GapStatus.PARTIAL, List.of(), "Some exposure.");

		GapItem verified = verify(item);

		assertThat(verified.status()).isEqualTo(GapStatus.GAP);
		assertThat(verified.note()).isEqualTo("Guardrail: marked GAP because the match cited no evidence.");
	}

	@Test
	void dropsOnlyTheUnknownIdsFromAGap() {
		var item = new GapItem("Java", GapStatus.GAP, List.of("exp-a-1", "exp-invented"), "Related work only.");

		assertThat(verify(item)).isEqualTo(new GapItem("Java", GapStatus.GAP, List.of("exp-a-1"), "Related work only."));
	}

	@Test
	void checksEveryItemAndKeepsTheirOrder() {
		var report = new GapReport(List.of(
				new GapItem("First", GapStatus.STRONG, List.of("proj-invented"), "x"),
				new GapItem("Second", GapStatus.STRONG, List.of("skill-angular"), "y")));

		GapReport verified = EvidenceGuardrail.verify(report, PROFILE_IDS);

		assertThat(verified.items()).extracting(GapItem::requirement).containsExactly("First", "Second");
		assertThat(verified.items()).extracting(GapItem::status).containsExactly(GapStatus.GAP, GapStatus.STRONG);
	}

	private static GapItem verify(GapItem item) {
		return EvidenceGuardrail.verify(new GapReport(List.of(item)), PROFILE_IDS).items().getFirst();
	}

}
