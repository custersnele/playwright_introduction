package be.pxl.solution.tests;

import be.pxl.solution.data.Order;
import be.pxl.solution.data.OrderType;
import be.pxl.solution.fixtures.GlobotOptions;
import be.pxl.solution.fixtures.WithOrder;
import be.pxl.solution.pages.BasketPage;
import be.pxl.solution.pages.CatalogPage;
import be.pxl.solution.pages.EventDetailPage;
import be.pxl.solution.pdf.PdfTicket;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end: a few UI tests that follow the whole journey, including the PDF download.
 * The detailed checks of the ticket content are in TC3/TC4: faster and without a browser.
 */
@UsePlaywright(GlobotOptions.class)
public class TC5_CheckoutTest {

	@Test
	@DisplayName("Order a ticket and download the PDF")
	void orderTicketAndDownloadPdf(Page page) throws IOException {
		CatalogPage catalog = new CatalogPage(page);
		catalog.navigate();
		assertThat(catalog.ticketCount()).hasText("0");

		EventDetailPage details = catalog.openEvent(2);
		assertThat(details.name()).hasText("To the Moon and Back");
		details.selectQuantity(3);

		BasketPage basket = details.placeOrder();
		assertThat(basket.total()).hasText("$405.00");

		Download download = basket.checkout("Zoë Łukasz");
		assertEquals("ticket.pdf", download.suggestedFilename());
		assertThat(basket.message()).hasText("Your ticket has been downloaded.");

		PdfTicket ticket = PdfTicket.from(Files.readAllBytes(download.path()));
		assertEquals("To the Moon and Back", ticket.valueOf("Event:"));
		assertEquals("Santiago Martinez", ticket.valueOf("Artist:"));
		assertEquals("Zoë Łukasz", ticket.valueOf("Customer:"));
		assertEquals("3", ticket.valueOf("Quantity:"));
		assertEquals("$405.00", ticket.valueOf("Total:"));
		assertTrue(PdfTicket.TICKET_NUMBER_FORMAT.matcher(ticket.ticketNumber()).matches(), ticket.ticketNumber());
	}

	@Test
	@DisplayName("The basket counter in the shared header follows the order on every page")
	void headerCounterOnEveryPage(Page page) {
		EventDetailPage details = new EventDetailPage(page);
		details.navigate(1);
		assertThat(details.ticketCount()).hasText("0");
		BasketPage basket = details.placeOrder();
		assertThat(basket.ticketCount()).hasText("1");

		basket.changeQuantity(4);
		assertThat(basket.ticketCount()).hasText("4");

		CatalogPage catalog = new CatalogPage(page);
		catalog.navigate();
		assertThat(catalog.ticketCount()).hasText("4");
		details.navigate(2);
		assertThat(details.ticketCount()).hasText("4");
	}

	@Test
	@DisplayName("Checkout without a name shows a message and downloads nothing")
	void checkoutRequiresName(Page page) {
		EventDetailPage details = new EventDetailPage(page);
		details.navigate(1);
		BasketPage basket = details.placeOrder();

		basket.clickCheckout();
		assertThat(basket.message()).hasText("Please enter the name on the ticket.");
	}

	@Test
	@DisplayName("A customer name of 81 characters is rejected by the service and the UI shows why")
	void tooLongNameShowsError(Page page) {
		EventDetailPage details = new EventDetailPage(page);
		details.navigate(1);
		BasketPage basket = details.placeOrder();

		basket.fillName("x".repeat(81));
		basket.clickCheckout();
		assertThat(basket.message()).containsText("customerName must be at most 80 characters");
	}
}
