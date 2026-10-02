package be.pxl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class FirstPlaywrightTest {

	private static final String URL = TestConfig.appUrl() + "catalog.html";

	@Test
	public void firstTest() {
		try (Playwright pw = Playwright.create()) { // guarantee the browser and pages will be closed in the end
			Browser browser = pw.chromium().launch(); // playwright runs in headless mode by default
			Page page = browser.newPage();
			page.navigate(URL);
			System.out.println("title " + page.title());
			assertThat(page).hasTitle("Globoticket");
		}
	}

	@Test
	public void differentBrowsers() {
		try (Playwright pw = Playwright.create()) {
			List<BrowserType> browserTypes = List.of(pw.chromium(), pw.firefox(), pw.webkit());
			for (BrowserType bt : browserTypes) {
				try (Browser browser = bt.launch()) {
					Page page = browser.newPage();
					page.navigate(URL);
					assertThat(page).hasTitle("Globoticket");
					page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target", "screenshots", bt.name() + ".png")));
				}
			}
		}
	}

}
