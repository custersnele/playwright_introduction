package be.pxl.solution.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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

	public Locator emptyMessage() {
		return page.getByText("No items in your basket.");
	}

	public Locator message() {
		return page.getByTestId("checkout-message");
	}

	@Step("Change the quantity to {quantity}")
	public void changeQuantity(int quantity) {
		page.getByTestId("quantity-dropdown").selectOption(String.valueOf(quantity));
		page.getByTestId("update-button").click();
	}

	@Step("Remove the event from the basket")
	public void remove() {
		page.getByTestId("remove-button").click();
	}

	/** Clicking "Back to event catalog" navigates, so the method returns the page you land on. */
	@Step("Go back to the catalog")
	public CatalogPage backToCatalog() {
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Back to event catalog")).click();
		return new CatalogPage(page);
	}

	public Locator checkoutButton() {
		return page.getByTestId("checkout-button");
	}

	@Step("Fill in the name '{customerName}'")
	public void fillName(String customerName) {
		page.getByTestId("customer-name").fill(customerName);
	}

	@Step("Click checkout")
	public void clickCheckout() {
		page.getByTestId("checkout-button").click();
	}

	/** Checkout triggers a file download: register the download listener before the click that causes it. */
	@Step("Check out as '{customerName}'")
	public Download checkout(String customerName) {
		fillName(customerName);
		return page.waitForDownload(this::clickCheckout);
	}
}
