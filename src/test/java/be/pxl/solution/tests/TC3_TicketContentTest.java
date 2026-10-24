package be.pxl.solution.tests;

import be.pxl.solution.api.TicketApi;
import be.pxl.solution.data.Order;
import be.pxl.solution.data.OrderFactory;
import be.pxl.solution.data.OrderType;
import be.pxl.solution.fixtures.GlobotOptions;
import be.pxl.solution.fixtures.WithOrder;
import be.pxl.solution.pdf.PdfTicket;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Component tests of the ticket service: no browser, just HTTP + PDF. Fast, and one test per kind of defect,
 * so a failing test name tells you which bug you found.
 */
@Tag(Tags.COMPONENT)
@Tag(Tags.PDF)
@Epic("Globoticket")
@Feature("Ticket content")
@UsePlaywright(GlobotOptions.class)
public class TC3_TicketContentTest {

	@ParameterizedTest
	@EnumSource(OrderType.class)
	@Issue("GLOBO-1")
	@DisplayName("Total is quantity x unit price")
	void totalIsCorrect(OrderType type, APIRequestContext request) {
		Order order = OrderFactory.create(type);
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(order.expectedTotal(), ticket.valueOf("Total:"));
	}

	@ParameterizedTest
	@EnumSource(OrderType.class)
	@DisplayName("VAT (21%, included in the total) is shown")
	void vatIsShown(OrderType type, APIRequestContext request) {
		Order order = OrderFactory.create(type);
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(order.expectedVat(), ticket.valueOf("VAT (21%):"));
	}

	@ParameterizedTest
	@EnumSource(OrderType.class)
	@DisplayName("Event name and customer name are printed in full")
	void namesAreNotTruncated(OrderType type, APIRequestContext request) {
		Order order = OrderFactory.create(type);
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		String text = ticket.normalizedText();
		assertTrue(text.contains(order.eventName()), "event name missing in: " + text);
		assertTrue(text.contains(order.customerName()), "customer name missing in: " + text);
	}

	@Test
	@DisplayName("Quantity, unit price and event details are printed")
	void basicFieldsArePrinted(@WithOrder(OrderType.GROUP) Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(String.valueOf(order.quantity()), ticket.valueOf("Quantity:"));
		assertEquals(order.expectedUnitPrice(), ticket.valueOf("Unit price:"));
		assertEquals(order.artist(), ticket.valueOf("Artist:"));
		assertEquals(order.eventDate(), ticket.valueOf("Event date:"));
	}

	@Test
	@DisplayName("Purchase date is dd/MM/yyyy HH:mm in Belgian time")
	void purchaseDateFormat(@WithOrder Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertEquals(order.expectedPurchased(), ticket.valueOf("Purchased:"));
	}

	@Test
	@DisplayName("Ticket number has the format GT-XXXXXXXX")
	void ticketNumberFormat(@WithOrder Order order, APIRequestContext request) {
		PdfTicket ticket = new TicketApi(request).createTicket(order);
		assertNotNull(ticket.ticketNumber(), "no ticket number found");
		assertTrue(PdfTicket.TICKET_NUMBER_FORMAT.matcher(ticket.ticketNumber()).matches(), ticket.ticketNumber());
	}

	@Test
	@DisplayName("Two tickets for the same event get different numbers")
	void ticketNumbersAreUnique(@WithOrder Order order, APIRequestContext request) {
		TicketApi api = new TicketApi(request);
		Set<String> numbers = new HashSet<>();
		for (int i = 0; i < 5; i++) {
			numbers.add(api.createTicket(order).ticketNumber());
		}
		assertEquals(5, numbers.size(), "ticket numbers: " + numbers);
	}

	@Test
	@DisplayName("Same order twice: only the ticket number differs, so tests can be deterministic")
	void sameInputGivesSameContent(@WithOrder Order order, APIRequestContext request) {
		TicketApi api = new TicketApi(request);
		PdfTicket first = api.createTicket(order);
		PdfTicket second = api.createTicket(order);
		assertNotEquals(first.ticketNumber(), second.ticketNumber());
		assertEquals(first.normalizedText().replace(first.ticketNumber(), ""),
				second.normalizedText().replace(second.ticketNumber(), ""));
	}

	@Test
	@DisplayName("A character without a glyph (emoji) does not break the ticket")
	void emojiIsAccepted(@WithOrder Order order, APIRequestContext request) {
		Order withEmoji = order.toBuilder().customerName("Sam 🎸 Rock").build();
		PdfTicket ticket = new TicketApi(request).createTicket(withEmoji);
		assertTrue(ticket.normalizedText().contains("Sam"), ticket.normalizedText());
	}

	@Test
	@DisplayName("Input validation: invalid orders are rejected with 400")
	void invalidOrdersAreRejected(@WithOrder Order order, APIRequestContext request) {
		TicketApi api = new TicketApi(request);
		assertAll400(api, "quantity 0", order.toBuilder().quantity(0).build());
		assertAll400(api, "quantity 11", order.toBuilder().quantity(11).build());
		assertAll400(api, "event name of 81 characters", order.toBuilder().eventName("x".repeat(81)).build());
		assertAll400(api, "3 decimals", order.toBuilder().unitPrice("10.999").build());
		assertAll400(api, "price 0", order.toBuilder().unitPrice("0").build());

		Map<String, Object> noCustomer = order.toRequestBody();
		noCustomer.remove("customerName");
		assertEquals(400, api.post(noCustomer).status());
	}

	@Test
	@DisplayName("Boundaries are accepted: quantity 1 and 10, name of 80 characters")
	void boundariesAreAccepted(@WithOrder Order order, APIRequestContext request) {
		TicketApi api = new TicketApi(request);
		assertEquals(200, api.post(order.toBuilder().quantity(1).build()).status());
		assertEquals(200, api.post(order.toBuilder().quantity(10).build()).status());
		assertEquals(200, api.post(order.toBuilder().eventName("x".repeat(80)).build()).status());
	}

	private static void assertAll400(TicketApi api, String description, Order order) {
		APIResponse response = api.post(order);
		assertEquals(400, response.status(), description + " should be rejected");
	}
}
