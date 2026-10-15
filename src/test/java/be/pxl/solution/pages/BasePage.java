package be.pxl.solution.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Everything the Globoticket pages have in common: the header with the logo and the basket counter.
 * Page objects return locators (not text) so the test decides what to assert and Playwright can auto-wait.
 */
public abstract class BasePage {

	protected final Page page;

	protected BasePage(Page page) {
		this.page = page;
	}

	/** Relative to the base URL configured in GlobotOptions (the app url, ending with a slash). */
	protected void open(String relativePath) {
		page.navigate(relativePath);
	}

	public Locator ticketCount() {
		return page.getByTestId("ticket-count");
	}

	public Locator logo() {
		return page.locator("header h1 img");
	}
}
