package io.github.ptitsynamv.cvtailor.documents;

import jakarta.validation.constraints.NotNull;

import io.github.ptitsynamv.cvtailor.gap.GapReport;
import io.github.ptitsynamv.cvtailor.job.JobAnalysis;

/** The results of the two previous steps, possibly edited by the user. */
public record DocumentsRequest(@NotNull JobAnalysis job, @NotNull GapReport gaps) {
}
