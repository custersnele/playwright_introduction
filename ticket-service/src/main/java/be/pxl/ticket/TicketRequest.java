package be.pxl.ticket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketRequest(
		String eventName,
		String artist,
		String eventDate,
		Integer quantity,
		BigDecimal unitPrice,
		String customerName,
		String purchasedAt) {

	public static final int MAX_NAME_LENGTH = 80;
	public static final int MAX_QUANTITY = 10;

	/** @return an error message, or null when the request is valid */
	public String validate() {
		if (isBlank(eventName)) return "eventName is required";
		if (tooLong(eventName, MAX_NAME_LENGTH)) return "eventName must be at most " + MAX_NAME_LENGTH + " characters";
		if (isBlank(customerName)) return "customerName is required";
		if (tooLong(customerName, MAX_NAME_LENGTH)) return "customerName must be at most " + MAX_NAME_LENGTH + " characters";
		if (artist != null && tooLong(artist, MAX_NAME_LENGTH)) return "artist must be at most " + MAX_NAME_LENGTH + " characters";
		if (eventDate != null && tooLong(eventDate, 40)) return "eventDate must be at most 40 characters";
		if (quantity == null) return "quantity is required";
		if (quantity < 1 || quantity > MAX_QUANTITY) return "quantity must be between 1 and " + MAX_QUANTITY;
		if (unitPrice == null) return "unitPrice is required";
		if (unitPrice.signum() <= 0) return "unitPrice must be greater than 0";
		if (unitPrice.compareTo(new BigDecimal("10000")) > 0) return "unitPrice must be at most 10000";
		if (unitPrice.scale() > 2) return "unitPrice can have at most 2 decimals";
		return null;
	}

	/** Counts code points, so a character outside the BMP (e.g. an emoji) counts as one. */
	private static boolean tooLong(String s, int max) {
		return s.codePointCount(0, s.length()) > max;
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}
