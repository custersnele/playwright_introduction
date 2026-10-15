package be.pxl.solution.data;

/** Factory: one named, deliberately chosen order per {@link OrderType}. Built on the builder with a sensible default. */
public final class OrderFactory {

	private OrderFactory() {
	}

	public static Order create(OrderType type) {
		Order.Builder builder = Order.builder();
		return switch (type) {
			case STANDARD -> builder.build();
			case VIP -> builder.eventName("To the Moon and Back").artist("Santiago Martinez")
					.quantity(2).unitPrice("1250.00").customerName("Victoria Vanderbilt").build();
			case GROUP -> builder.eventName("The State of Affairs: Mariam Live!").artist("Mariam Johnson")
					.quantity(4).unitPrice("85").customerName("Groep Scouts Hasselt").build();
			case LONG_NAME -> builder
					.eventName("The Very Long Evening of Improvised Jazz, Poetry and Fireworks at the Old Docks!")
					.build();
			case UNICODE_NAME -> builder.eventName("Beyoncé ★ Münster ♪").artist("Zoë Łukasz")
					.customerName("Zoë Łukasz-Müller").build();
			case CENTS_PRICE -> builder.quantity(3).unitPrice("63.33").build();
			case MAX_QUANTITY -> builder.quantity(10).unitPrice("19.99").build();
		};
	}
}
