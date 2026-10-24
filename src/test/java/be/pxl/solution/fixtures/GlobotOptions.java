package be.pxl.solution.fixtures;

import be.pxl.TestConfig;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

import java.net.URI;
import java.nio.file.Path;

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
				// A trace zip per test in target/test-results, deleted again when the test passes
				.setOutputDir(Path.of("target", "test-results"))
				.setTrace(trace())
				// REST calls go to the server root (the ticket service lives under /api/, next to /app/)
				.setApiRequestOptions(new APIRequest.NewContextOptions().setBaseURL(serverRoot()));
		if (browser.equals("chrome")) {
			options.setBrowserName("chromium").setChannel("chrome");
		} else {
			options.setBrowserName(browser);
		}
		return options;
	}

	/** -Dtrace=on|off|retain-on-failure (default). Open a zip with: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace <zip>" */
	private static Options.Trace trace() {
		return switch (System.getProperty("trace", "retain-on-failure")) {
			case "on" -> Options.Trace.ON;
			case "off" -> Options.Trace.OFF;
			default -> Options.Trace.RETAIN_ON_FAILURE;
		};
	}

	private static String serverRoot() {
		URI uri = URI.create(TestConfig.appUrl());
		return uri.getScheme() + "://" + uri.getAuthority();
	}
}
