package be.pxl.comparison;

import be.pxl.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Slide material: the same scenario as FilterEventsSeleniumTest, written with Playwright.
 */
class FilterEventsPlaywrightTest {

	private Playwright playwright;
	private Browser browser;

	@BeforeEach
	void startBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(TestConfig.headless()));
	}

	@AfterEach
	void stopBrowser() {
		playwright.close();
	}

	@Test
	void filterShowsOnlyMatchingEvent() {
		Page page = browser.newPage();
		page.navigate(TestConfig.appUrl() + "catalog.html");

		page.getByTestId("filter-text").fill("moon");
		page.getByTestId("filter-button").click();

		// assertions retry until they pass or time out: no explicit wait
		assertThat(page.locator("tbody tr")).hasCount(1);
		assertThat(page.getByTestId("event-name-2")).hasText("To the Moon and Back");
	}
}
