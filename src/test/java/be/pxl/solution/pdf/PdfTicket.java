package be.pxl.solution.pdf;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Test helper that looks inside a ticket PDF: text, metadata, fonts, rendered image, QR code. */
public class PdfTicket {

	public static final Pattern TICKET_NUMBER_FORMAT = Pattern.compile("GT-[A-HJ-NP-Z2-9]{8}");

	private final byte[] bytes;
	private final String text;

	private PdfTicket(byte[] bytes, String text) {
		this.bytes = bytes;
		this.text = text;
	}

	public static PdfTicket from(byte[] bytes) {
		try (PDDocument doc = Loader.loadPDF(bytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			return new PdfTicket(bytes, stripper.getText(doc));
		} catch (IOException e) {
			throw new UncheckedIOException("Not a readable PDF", e);
		}
	}

	/** Raw extracted text, one line per visual line. */
	public String text() {
		return text;
	}

	/** Extracted text with all whitespace runs (including line breaks from wrapping) collapsed to one space. */
	public String normalizedText() {
		return text.replaceAll("\\s+", " ").trim();
	}

	/** The first line starting with the label, without the label, e.g. valueOf("Total:") gives "$189.99". */
	public String valueOf(String label) {
		return text.lines()
				.map(String::strip)
				.filter(line -> line.startsWith(label))
				.map(line -> line.substring(label.length()).strip())
				.findFirst()
				.orElse(null);
	}

	public boolean hasLabel(String label) {
		return valueOf(label) != null;
	}

	public String ticketNumber() {
		Matcher m = Pattern.compile("Ticket no:\\s*(\\S+)").matcher(text);
		return m.find() ? m.group(1) : null;
	}

	public int pageCount() {
		return withDocument(PDDocument::getNumberOfPages);
	}

	public String title() {
		return withDocument(doc -> doc.getDocumentInformation().getTitle());
	}

	/** Names of the fonts used on page 1, with "embedded" or "not embedded". */
	public List<String> fonts() {
		return withDocument(doc -> {
			List<String> fonts = new ArrayList<>();
			for (COSName name : doc.getPage(0).getResources().getFontNames()) {
				PDFont font = doc.getPage(0).getResources().getFont(name);
				fonts.add(font.getName() + (font.isEmbedded() ? " (embedded)" : " (not embedded)"));
			}
			return fonts;
		});
	}

	public BufferedImage render(float dpi) {
		return withDocument(doc -> new PDFRenderer(doc).renderImageWithDPI(0, dpi));
	}

	/** Renders the page and decodes the QR code. */
	public String qrContent() {
		BufferedImage image = render(150);
		try {
			return new MultiFormatReader()
					.decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))))
					.getText();
		} catch (NotFoundException e) {
			return null;
		}
	}

	public byte[] bytes() {
		return bytes;
	}

	private <T> T withDocument(DocumentFunction<T> function) {
		try (PDDocument doc = Loader.loadPDF(bytes)) {
			return function.apply(doc);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private interface DocumentFunction<T> {
		T apply(PDDocument doc) throws IOException;
	}
}
