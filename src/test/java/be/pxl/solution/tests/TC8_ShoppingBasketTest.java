package be.pxl.solution.tests;

import be.pxl.solution.fixtures.GlobotOptions;
import be.pxl.solution.pages.BasketPage;
import be.pxl.solution.pages.CatalogPage;
import be.pxl.solution.pages.EventDetailPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**

 */
@Tag(Tags.E2E)
@Epic("Globoticket")
@Feature("Shopping basket")
@UsePlaywright(GlobotOptions.class)
public class TC8_ShoppingBasketTest {

	@Test
	@DisplayName("")
	void checkShoppingBasketCounter(Page page) {
		CatalogPage catalogPage = new CatalogPage(page);
		catalogPage.navigate();
		EventDetailPage eventDetailPage = catalogPage.openEvent(2);
		eventDetailPage.selectQuantity(4);
		BasketPage basketPage = eventDetailPage.placeOrder();
		assertThat(basketPage.ticketCount()).hasText("4");
		catalogPage = basketPage.backToCatalog();
		assertThat(catalogPage.ticketCount()).hasText("4");
	}

}
