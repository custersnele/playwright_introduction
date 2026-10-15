package be.pxl.solution.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class BasketPage extends BasePage {

	public BasketPage(Page page) {
		super(page);
	}

	public Locator eventName() {
		return page.getByTestId("event-name");
	}

	public Locator price() {
		return page.getByTestId("event-price");
	}

	public Locator total() {
		return page.getByTestId("event-total");
	}

	public Locator message() {
		return page.getByTestId("checkout-message");
	}

	public void changeQuantity(int quantity) {
		page.getByTestId("quantity-dropdown").selectOption(String.valueOf(quantity));
		page.getByTestId("update-button").click();
	}

	public void fillName(String customerName) {
		page.getByTestId("customer-name").fill(customerName);
	}

	public void clickCheckout() {
		page.getByTestId("checkout-button").click();
	}

	/** Checkout triggers a file download: register the download listener before the click that causes it. */
	public Download checkout(String customerName) {
		fillName(customerName);
		return page.waitForDownload(this::clickCheckout);
	}
}
