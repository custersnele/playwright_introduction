package be.pxl.solution.fixtures;

import be.pxl.TestConfig;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

import java.net.URI;

/**
 * One place that configures every Playwright fixture (Playwright, Browser, BrowserContext, Page, APIRequestContext).
 * Use it with @UsePlaywright(GlobotOptions.class).
 */
public class GlobotOptions implements OptionsFactory {

	@Override
	public Options getOptions() {
		String browser = System.getProperty("browser.type", "chromium");
		Options options = new Options()
				.setBaseUrl(TestConfig.appUrl())
				.setHeadless(TestConfig.headless())
				// REST calls go to the server root (the ticket service lives under /api/, next to /app/)
				.setApiRequestOptions(new APIRequest.NewContextOptions().setBaseURL(serverRoot()));
		if (browser.equals("chrome")) {
			options.setBrowserName("chromium").setChannel("chrome");
		} else {
			options.setBrowserName(browser);
		}
		return options;
	}

	private static String serverRoot() {
		URI uri = URI.create(TestConfig.appUrl());
		return uri.getScheme() + "://" + uri.getAuthority();
	}
}
