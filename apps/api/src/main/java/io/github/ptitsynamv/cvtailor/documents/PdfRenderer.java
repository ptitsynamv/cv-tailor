package io.github.ptitsynamv.cvtailor.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Thymeleaf template to PDF. Noto Sans (plus Noto Sans Math as a per-glyph fallback) is embedded because the
 * built-in PDF fonts can't show characters such as "–", "’" or "→" that real profiles contain.
 */
@Service
public class PdfRenderer {

	static final String FONT_FAMILY = "Noto Sans";

	private final ITemplateEngine templateEngine;

	public PdfRenderer(ITemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	/** @param template a template under {@code templates/}, e.g. {@code pdf/cv} */
	public byte[] render(String template, Map<String, Object> variables) {
		String html = templateEngine.process(template, new Context(Locale.ENGLISH, variables));
		// jsoup turns Thymeleaf's HTML output into the well-formed DOM the PDF renderer needs.
		var document = W3CDom.convert(Jsoup.parse(html));

		var pdf = new ByteArrayOutputStream();
		try {
			new PdfRendererBuilder()
					.useFastMode()
					.usePdfUaAccessibility(true)
					.useFont(font("NotoSans-Regular.ttf"), FONT_FAMILY, 400, FontStyle.NORMAL, true)
					.useFont(font("NotoSans-Bold.ttf"), FONT_FAMILY, 700, FontStyle.NORMAL, true)
					// Fallback for glyphs Noto Sans lacks, such as arrows ("Angular 15 → 19").
					.useFont(font("NotoSansMath-Regular.ttf"), "Noto Sans Math", 400, FontStyle.NORMAL, true)
					.withW3cDocument(document, "/")
					.toStream(pdf)
					.run();
		}
		catch (IOException e) {
			throw new UncheckedIOException("Can't render " + template + " to PDF", e);
		}
		return pdf.toByteArray();
	}

	private static FSSupplier<InputStream> font(String file) {
		return () -> Objects.requireNonNull(PdfRenderer.class.getResourceAsStream("/fonts/" + file), file);
	}

}
