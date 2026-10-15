package be.pxl.solution.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class EventDetailPage extends BasePage {

	public EventDetailPage(Page page) {
		super(page);
	}

	public void navigate(int eventId) {
		open("details.html?id=" + eventId);
	}

	public Locator name() {
		return page.getByTestId("event-name");
	}

	public Locator artist() {
		return page.getByTestId("event-artist");
	}

	public Locator date() {
		return page.getByTestId("event-date");
	}

	public Locator price() {
		return page.getByTestId("event-price");
	}

	public void selectQuantity(int quantity) {
		page.getByTestId("quantity").selectOption(String.valueOf(quantity));
	}

	public BasketPage placeOrder() {
		page.getByTestId("place-order").click();
		return new BasketPage(page);
	}
}
