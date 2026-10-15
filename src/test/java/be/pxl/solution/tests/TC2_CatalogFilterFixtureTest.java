package be.pxl.solution.tests;

import be.pxl.solution.fixtures.GlobotOptions;
import be.pxl.solution.pages.CatalogPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Same scenarios as TC1_CatalogFilterTest, without @BeforeAll/@AfterAll/@BeforeEach/@AfterEach:
 * the Playwright fixtures create and close Playwright, Browser, BrowserContext and Page for us.
 * (TC1 spends more than 20 lines on lifecycle code; this class has none.)
 */
@UsePlaywright(GlobotOptions.class)
public class TC2_CatalogFilterFixtureTest {

	@Test
	@DisplayName("Filter catalog and verify event")
	void catalogFilterTest(Page page) {
		CatalogPage catalogPage = new CatalogPage(page);
		catalogPage.navigate();
		catalogPage.search("moon");

		assertThat(catalogPage.events()).hasCount(1);
		assertThat(catalogPage.eventName(2)).isVisible();
		assertThat(catalogPage.eventName(2)).hasText("To the Moon and Back");
	}

	@Test
	@DisplayName("Filter without results")
	void catalogFilterWithoutResultsTest(Page page) {
		CatalogPage catalogPage = new CatalogPage(page);
		catalogPage.navigate();
		catalogPage.search("hocus pocus");

		assertThat(catalogPage.events()).hasCount(0);
	}
}
