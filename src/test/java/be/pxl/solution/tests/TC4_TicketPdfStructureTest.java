package be.pxl.solution.tests;

import be.pxl.solution.api.TicketApi;
import be.pxl.solution.data.Order;
import be.pxl.solution.data.OrderFactory;
import be.pxl.solution.data.OrderType;
import be.pxl.solution.fixtures.GlobotOptions;
import be.pxl.solution.fixtures.WithOrder;
import be.pxl.solution.pdf.ImageDiff;
import be.pxl.solution.pdf.PdfTicket;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Looking deeper into the PDF: metadata, fonts, QR code, and a pixel comparison with a golden image. */
@Tag(Tags.COMPONENT)
@Tag(Tags.PDF)
@Epic("Globoticket")
@Feature("Ticket PDF structure")
@UsePlaywright(GlobotOptions.class)
public class TC4_TicketPdfStructureTest {

	private static final float DPI = 100;
	private static final Path GOLDEN = Path.of("src/test/resources/golden/ticket-standard.png");

	@Test
	@DisplayName("One A4 page with the ticket number in the document title")
	void pageCountAndTitle(@WithOrder Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(1, ticket.pageCount());
		assertEquals("Globoticket ticket " + ticket.ticketNumber(), ticket.title());
	}

	@Test
	@DisplayName("Fonts are embedded, otherwise ü and ★ depend on the reader's computer")
	void fontsAreEmbedded(@WithOrder(OrderType.UNICODE_NAME) Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertTrue(ticket.fonts().stream().allMatch(f -> f.endsWith("(embedded)")), ticket.fonts().toString());
		assertTrue(ticket.normalizedText().contains(order.eventName()), ticket.normalizedText());
	}

	@Test
	@DisplayName("The QR code contains the printed ticket number")
	void qrCodeMatchesTicketNumber(@WithOrder Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(ticket.ticketNumber(), ticket.qrContent());
	}

	/**
	 * Visual regression: render the page and compare it with a golden image. The ticket number and the QR code
	 * change on every run, so those regions are masked (like the mask option of Playwright's toHaveScreenshot).
	 * Create or refresh the golden image against a correct service with: mvn test -Dtest=TC4* -Dupdate.golden=true
	 */
	@Test
	@DisplayName("Layout matches the golden image")
	void layoutMatchesGoldenImage(APIRequestContext request) throws IOException {
		Order order = OrderFactory.create(OrderType.STANDARD);
		BufferedImage actual = ImageDiff.mask(new TicketApi(request).createTicket(order).render(DPI), maskedRegions());

		if (Boolean.getBoolean("update.golden") || !Files.exists(GOLDEN)) {
			Files.createDirectories(GOLDEN.getParent());
			ImageIO.write(actual, "png", GOLDEN.toFile());
			fail("Golden image written to " + GOLDEN + ": review it and run the test again.");
		}

		BufferedImage expected = ImageIO.read(GOLDEN.toFile());
		double ratio = ImageDiff.differingPixelRatio(actual, expected, 40);
		if (ratio > 0.001) {
			Path out = Path.of("target/visual");
			Files.createDirectories(out);
			ImageIO.write(actual, "png", out.resolve("ticket-actual.png").toFile());
			if (actual.getWidth() == expected.getWidth() && actual.getHeight() == expected.getHeight()) {
				ImageIO.write(ImageDiff.highlight(actual, expected, 40), "png", out.resolve("ticket-diff.png").toFile());
			}
			fail("%.2f%% of the pixels differ from the golden image, see %s".formatted(ratio * 100, out.toAbsolutePath()));
		}
	}

	/** Page regions that legitimately change (points from the top-left, scaled to the render DPI). */
	private static List<Rectangle> maskedRegions() {
		float scale = DPI / 72;
		return List.of(
				scaled(45, 85, 285, 35, scale),   // "Ticket no: GT-XXXXXXXX"
				scaled(440, 45, 110, 110, scale)); // QR code
	}

	private static Rectangle scaled(float x, float y, float w, float h, float scale) {
		return new Rectangle(Math.round(x * scale), Math.round(y * scale), Math.round(w * scale), Math.round(h * scale));
	}
}
