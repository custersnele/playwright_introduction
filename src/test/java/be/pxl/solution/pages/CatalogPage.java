package be.pxl.solution.pages;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import com.microsoft.playwright.Page;

public class CatalogPage extends BasePage {

	private final Locator searchInput;
	private final Locator filterButton;
	private final Locator events;

	public CatalogPage(Page page) {
		super(page);
		this.searchInput = page.getByTestId("filter-text");
		this.filterButton = page.getByTestId("filter-button");
		this.events = page.locator("#event-list").locator("tr");
	}

	@Step("Open the catalog")
	public void navigate() {
		open("catalog.html");
	}

	@Step("Search events for '{searchText}'")
	public void search(String searchText) {
		searchInput.fill(searchText);
		filterButton.click();
	}

	public Locator events() {
		return events;
	}

	public Locator eventName(int eventId) {
		return page.getByTestId("event-name-" + eventId);
	}

	public Locator eventPrice(int eventId) {
		return page.getByTestId("event-price-" + eventId);
	}

	/** Clicking "Purchase Details" navigates, so the method returns the page you land on. */
	@Step("Open the details of event {eventId}")
	public EventDetailPage openEvent(int eventId) {
		page.getByTestId("purchase-button-" + eventId).click();
		return new EventDetailPage(page);
	}
}
