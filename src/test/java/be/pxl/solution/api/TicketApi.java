package be.pxl.solution.api;

import be.pxl.solution.data.Order;
import be.pxl.solution.pdf.PdfTicket;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Thin client around POST /api/tickets. The leading slash makes the path relative to the server root. */
public class TicketApi {

	private final APIRequestContext request;

	public TicketApi(APIRequestContext request) {
		this.request = request;
	}

	public APIResponse post(Object body) {
		return request.post("/api/tickets", RequestOptions.create().setData(body));
	}

	public APIResponse post(Order order) {
		return post(order.toRequestBody());
	}

	/** Creates a ticket and parses the PDF; fails the test when the service does not answer 200. */
	public PdfTicket createTicket(Order order) {
		APIResponse response = post(order);
		assertEquals(200, response.status(), () -> "Unexpected response: " + response.text());
		return PdfTicket.from(response.body());
	}
}
