package be.pxl.ticket;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders one A4 ticket. Layout (points, origin bottom-left):
 * header top-left, ticket number below it, QR code of the ticket number top-right (x 445-545, y 690-790),
 * then one "Label: value" row per field (label at x=50, value at x=190).
 */
public class TicketPdfGenerator {

	private static final ZoneId ZONE = ZoneId.of("Europe/Brussels");
	private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
	private static final float LABEL_X = 50;
	private static final float VALUE_X = 190;
	private static final float VALUE_WIDTH = 595 - 50 - VALUE_X;
	private static final float SIZE = 11;
	private static final float LEADING = 15;

	private final Set<Bugs> bugs;

	public TicketPdfGenerator(Set<Bugs> bugs) {
		this.bugs = bugs;
	}

	public byte[] generate(TicketRequest r) throws IOException {
		Instant purchasedAt = r.purchasedAt() == null || r.purchasedAt().isBlank() ? Instant.now() : Instant.parse(r.purchasedAt());
		String ticketNumber = TicketNumbers.next(r, bugs);

		BigDecimal total = r.unitPrice().multiply(BigDecimal.valueOf(r.quantity()));
		if (bugs.contains(Bugs.TOTAL)) {
			total = total.setScale(0, RoundingMode.DOWN);
		}
		total = total.setScale(2, RoundingMode.HALF_UP);
		BigDecimal vat = total.subtract(total.divide(BigDecimal.ONE.add(VAT_RATE), 2, RoundingMode.HALF_UP));

		String eventName = bugs.contains(Bugs.TRUNCATE) && r.eventName().length() > 30 ? r.eventName().substring(0, 30) : r.eventName();
		String datePattern = bugs.contains(Bugs.DATE) ? "MM/dd/yyyy HH:mm" : "dd/MM/yyyy HH:mm";
		String purchased = ZonedDateTime.ofInstant(purchasedAt, ZONE).format(DateTimeFormatter.ofPattern(datePattern));

		try (PDDocument doc = new PDDocument()) {
			PDDocumentInformation info = doc.getDocumentInformation();
			info.setTitle("Globoticket ticket " + ticketNumber);
			info.setAuthor("Globoticket");
			info.setCreationDate(GregorianCalendar.from(ZonedDateTime.ofInstant(purchasedAt, ZONE)));
			PDFont regular = loadFont(doc, "/fonts/DejaVuSans.ttf");
			PDFont bold = loadFont(doc, "/fonts/DejaVuSans-Bold.ttf");

			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				text(cs, bold, 24, LABEL_X, 770, "GLOBOTICKET");
				text(cs, bold, 14, LABEL_X, 735, "Ticket no: " + ticketNumber);
				drawQr(cs, ticketNumber, 445, 690, 100);

				float y = 650;
				y = row(cs, regular, bold, y, "Event:", eventName);
				y = row(cs, regular, bold, y, "Artist:", r.artist() == null ? "" : r.artist());
				y = row(cs, regular, bold, y, "Event date:", r.eventDate() == null ? "" : r.eventDate());
				y = row(cs, regular, bold, y, "Customer:", r.customerName());
				y = row(cs, regular, bold, y, "Quantity:", String.valueOf(r.quantity()));
				y = row(cs, regular, bold, y, "Unit price:", money(r.unitPrice()));
				y = row(cs, regular, bold, y, "Total:", money(total));
				if (!bugs.contains(Bugs.VAT)) {
					y = row(cs, regular, bold, y, "VAT (21%):", money(vat));
				}
				row(cs, regular, bold, y, "Purchased:", purchased);
				text(cs, regular, 9, LABEL_X, 60, "Prices include VAT. Keep this ticket and show the QR code at the entrance.");
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.save(out);
			return out.toByteArray();
		}
	}

	private static PDFont loadFont(PDDocument doc, String resource) throws IOException {
		try (InputStream in = TicketPdfGenerator.class.getResourceAsStream(resource)) {
			return PDType0Font.load(doc, in, true);
		}
	}

	private static String money(BigDecimal amount) {
		return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	private float row(PDPageContentStream cs, PDFont regular, PDFont bold, float y, String label, String value) throws IOException {
		text(cs, bold, SIZE, LABEL_X, y, label);
		List<String> lines = wrap(regular, SIZE, safe(regular, value), VALUE_WIDTH);
		float lineY = y;
		for (String line : lines) {
			text(cs, regular, SIZE, VALUE_X, lineY, line);
			lineY -= LEADING;
		}
		return lineY - 8;
	}

	private static void text(PDPageContentStream cs, PDFont font, float size, float x, float y, String s) throws IOException {
		if (s.isEmpty()) return;
		cs.beginText();
		cs.setFont(font, size);
		cs.newLineAtOffset(x, y);
		cs.showText(safe(font, s));
		cs.endText();
	}

	/** Characters the font has no glyph for (e.g. colour emoji) are replaced by '?'. */
	private static String safe(PDFont font, String s) {
		StringBuilder sb = new StringBuilder();
		s.codePoints().forEach(cp -> {
			String ch = new String(Character.toChars(cp));
			try {
				font.encode(ch);
				sb.append(ch);
			} catch (Exception e) {
				sb.append('?');
			}
		});
		return sb.toString();
	}

	private static List<String> wrap(PDFont font, float size, String text, float maxWidth) throws IOException {
		List<String> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = current.isEmpty() ? word : current + " " + word;
			if (width(font, size, candidate) <= maxWidth) {
				current = new StringBuilder(candidate);
				continue;
			}
			if (!current.isEmpty()) {
				lines.add(current.toString());
				current = new StringBuilder();
			}
			// a single word wider than the column: break it by characters
			String rest = word;
			while (width(font, size, rest) > maxWidth) {
				int cut = rest.length();
				while (cut > 1 && width(font, size, rest.substring(0, cut)) > maxWidth) cut--;
				lines.add(rest.substring(0, cut));
				rest = rest.substring(cut);
			}
			current = new StringBuilder(rest);
		}
		lines.add(current.toString());
		return lines;
	}

	private static float width(PDFont font, float size, String s) throws IOException {
		return font.getStringWidth(s) / 1000 * size;
	}

	private static void drawQr(PDPageContentStream cs, String content, float x, float y, float size) throws IOException {
		try {
			BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, Map.of(EncodeHintType.MARGIN, 0));
			float module = size / matrix.getWidth();
			for (int row = 0; row < matrix.getHeight(); row++) {
				for (int col = 0; col < matrix.getWidth(); col++) {
					if (matrix.get(col, row)) {
						cs.addRect(x + col * module, y + size - (row + 1) * module, module, module);
					}
				}
			}
			cs.fill();
		} catch (com.google.zxing.WriterException e) {
			throw new IOException(e);
		}
	}
}
