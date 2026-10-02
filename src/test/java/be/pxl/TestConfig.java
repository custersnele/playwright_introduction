package be.pxl;

/**
 * Central place for settings that differ per machine or environment.
 * Override with system properties, e.g. mvn test -Dapp.url=https://my-server/app/ -Dheadless=false
 */
public final class TestConfig {

	private TestConfig() {
	}

	public static String appUrl() {
		return System.getProperty("app.url", "http://localhost:5001/app/");
	}

	public static boolean headless() {
		return Boolean.parseBoolean(System.getProperty("headless", "true"));
	}
}
