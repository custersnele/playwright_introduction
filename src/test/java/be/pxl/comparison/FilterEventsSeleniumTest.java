package be.pxl.comparison;

import be.pxl.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Slide material: the same scenario as FilterEventsPlaywrightTest, written with Selenium.
 */
class FilterEventsSeleniumTest {

	private WebDriver driver;

	@BeforeEach
	void startBrowser() {
		ChromeOptions options = new ChromeOptions();
		if (TestConfig.headless()) {
			options.addArguments("--headless=new");
		}
		driver = new ChromeDriver(options);
	}

	@AfterEach
	void stopBrowser() {
		driver.quit();
	}

	@Test
	void filterShowsOnlyMatchingEvent() {
		driver.get(TestConfig.appUrl() + "catalog.html");

		driver.findElement(By.cssSelector("[data-testid='filter-text']")).sendKeys("moon");
		driver.findElement(By.cssSelector("[data-testid='filter-button']")).click();

		// explicit wait needed, otherwise we may read the rows before the table is re-rendered
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("tbody tr"), 1));

		List<?> rows = driver.findElements(By.cssSelector("tbody tr"));
		assertEquals(1, rows.size());
		assertEquals("To the Moon and Back",
				driver.findElement(By.cssSelector("[data-testid='event-name-2']")).getText());
	}
}
