package be.pxl.best.practices.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class BrowserFactory {

	public static Browser createBrowser(Playwright playwright, boolean headless) {
		String browserType = System.getProperty("browser.type");
		return switch (browserType) {
			case "chromium" ->
					playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
			case "chrome" ->
					playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(headless));
			case "firefox" ->
					playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(headless));
			case "webkit" ->
					playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(headless));
			default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
		};
	}

	public static Browser createBrowser(Playwright playwright) {
		return createBrowser(playwright, true);
	}
}
