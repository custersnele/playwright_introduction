package be.pxl.best.practices.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CatalogPage {
	private static final String PAGE = "catalog.html";
	private static final String URL = System.getProperty("app.url") + PAGE;
	private final Page page;

	// Define locators for the elements
	private final Locator searchInput;
	private final Locator filterButton;
	private final Locator eventList;
	private final Locator events;

	// Constructor to initialize the Page and locators
	public CatalogPage(Page page) {
		this.page = page;

		// Initialize the locators
		this.searchInput = page.getByTestId("filter-text");
		this.filterButton = page.getByTestId("filter-button");
		this.eventList = page.locator("#event-list");
		this.events = page.locator("tbody").locator("tr");
	}

	public void navigate() {
		page.navigate(URL);
	}

	public void search(String searchText) {
		searchInput.fill(searchText);
		filterButton.click();
	}

	// Method to get the event list (returns event rows)
	public String getEventList() {
		return eventList.innerHTML();
	}

	public int countEvents() {
		return events.count();
	}

	// Method to check if events are present
	public boolean isEventListEmpty() {
		return events.count() == 0;
	}

	public String getEventName(int eventId) {
		return page.getByTestId("event-name-" + eventId).innerText();
	}

	public void verifyEventVisible(int eventId) {
		assertThat(page.getByTestId("event-name-" + eventId)).isVisible();
	}

	public void verifyEventName(int eventId, String expectedName) {
		assertThat(page.getByTestId("event-name-" + eventId)).hasText(expectedName);
	}
}
