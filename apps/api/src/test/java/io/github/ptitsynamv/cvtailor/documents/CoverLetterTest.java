package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class CoverLetterTest {

	@Test
	void countsWordsAcrossParagraphs() {
		var letter = new CoverLetter("Dear Hiring Team,", List.of("One two three.", "Four five."));

		assertThat(letter.wordCount()).isEqualTo(5);
	}

	@Test
	void ignoresBlankParagraphsAndSurroundingWhitespace() {
		var letter = new CoverLetter("Dear Hiring Team,", List.of("", "  One two  ", "   "));

		assertThat(letter.wordCount()).isEqualTo(2);
	}

}
