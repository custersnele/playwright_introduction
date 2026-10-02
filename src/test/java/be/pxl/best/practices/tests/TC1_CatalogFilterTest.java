package be.pxl.best.practices.tests;

import be.pxl.best.practices.utils.BrowserFactory;
import be.pxl.best.practices.pages.CatalogPage;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TC1_CatalogFilterTest {

	// Shared between all tests in this class.
	private static Playwright playwright;
	private static Browser browser;

	// New instance for each test method.
	private BrowserContext context;
	private CatalogPage catalogPage;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = BrowserFactory.createBrowser(playwright, false);
	}

	@AfterAll
	static void closeBrowser() {
		browser.close();
		playwright.close();
	}

	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext();
		catalogPage = new CatalogPage(context.newPage());
	}

	@AfterEach
	void closeContext() {
		context.close();
	}


	@Test
	@DisplayName("Filter catalog and verify event")
	public void catalogFilterTest() {
		catalogPage.navigate();
		catalogPage.search("moon");
		assertEquals(1, catalogPage.countEvents());
		catalogPage.verifyEventVisible(2);
		catalogPage.verifyEventName(2, "To the Moon and Back");
		assertEquals("To The Moon And Back", catalogPage.getEventName(2));
	}

	@Test
	@DisplayName("Filter without results")
	public void catalogFilterWithoutResultsTest() {
		catalogPage.navigate();
		catalogPage.search("hocus pocus");
		assertTrue(catalogPage.isEventListEmpty());
	}
}
