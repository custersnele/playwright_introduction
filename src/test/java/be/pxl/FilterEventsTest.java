package be.pxl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class FilterEventsTest {

	// Shared between all tests in this class.
	private static Playwright playwright;
	private static Browser browser;
	private static final String URL = TestConfig.appUrl() + "catalog.html";

	// New instance for each test method.
	private BrowserContext context;
	private Page page;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(TestConfig.headless());
		browser = playwright.chromium().launch(options);
	}

	@AfterAll
	static void closeBrowser() {
		playwright.close();
	}

	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext();
		page = context.newPage();
		page.navigate(URL);
	}

	@AfterEach
	void closeContext() {
		context.close();
	}

	private void filterOn(String text) {
		page.getByTestId("filter-text").fill(text);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filter")).click();
	}

	@Test
	void filterShowsOnlyMatchingEvent() {
		filterOn("moon");

		Locator rows = page.locator("tbody").locator("tr");
		assertThat(rows).hasCount(1);
		assertThat(page.getByTestId("event-name-2")).hasText("To the Moon and Back");
	}

	@Test
	void eventNameIsCapitalizedByCss() {
		filterOn("moon");

		// the text in the DOM is unchanged, only the rendering is capitalized
		assertThat(page.getByTestId("event-name-2")).hasCSS("text-transform", "capitalize");
	}

	@Test
	void filterWithoutMatchShowsNoEvents() {
		filterOn("hocus pocus");

		assertThat(page.locator("tbody").locator("tr")).hasCount(0);
	}

	// Exercise: the filter lowercases the event name but not the search text. Fix main.js and enable this test.
	@Test
	@Disabled("Known bug: filter is case sensitive for the search text")
	void filterIsCaseInsensitive() {
		filterOn("Moon");

		assertThat(page.locator("tbody").locator("tr")).hasCount(1);
	}
}
