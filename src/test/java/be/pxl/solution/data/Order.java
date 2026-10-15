package be.pxl.solution.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Test data for POST /api/tickets, plus the values a correct ticket must show (the "oracle"). */
public record Order(
		String eventName,
		String artist,
		String eventDate,
		int quantity,
		BigDecimal unitPrice,
		String customerName,
		Instant purchasedAt) {

	private static final ZoneId BRUSSELS = ZoneId.of("Europe/Brussels");

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		return new Builder().eventName(eventName).artist(artist).eventDate(eventDate).quantity(quantity)
				.unitPrice(unitPrice.toPlainString()).customerName(customerName).purchasedAt(purchasedAt);
	}

	public Map<String, Object> toRequestBody() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("eventName", eventName);
		body.put("artist", artist);
		body.put("eventDate", eventDate);
		body.put("quantity", quantity);
		body.put("unitPrice", unitPrice);
		body.put("customerName", customerName);
		body.put("purchasedAt", purchasedAt.toString());
		return body;
	}

	public String expectedTotal() {
		return money(total());
	}

	/** Prices include 21% VAT. */
	public String expectedVat() {
		BigDecimal total = total();
		return money(total.subtract(total.divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP)));
	}

	public String expectedUnitPrice() {
		return money(unitPrice);
	}

	/** Day first, in Belgian time. */
	public String expectedPurchased() {
		return ZonedDateTime.ofInstant(purchasedAt, BRUSSELS).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	private BigDecimal total() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	private static String money(BigDecimal amount) {
		return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	public static class Builder {
		private String eventName = "Alexander Lemtov Live";
		private String artist = "Alexander Lemtov";
		private String eventDate = "1/11/2024 4:22 PM";
		private int quantity = 2;
		private BigDecimal unitPrice = new BigDecimal("65");
		private String customerName = "Ann Peeters";
		// Fixed on purpose: a test that depends on "now" is a flaky test. 24 > 12, so day/month mix-ups are visible.
		private Instant purchasedAt = Instant.parse("2026-07-24T12:30:00Z");

		public Builder eventName(String v) { this.eventName = v; return this; }
		public Builder artist(String v) { this.artist = v; return this; }
		public Builder eventDate(String v) { this.eventDate = v; return this; }
		public Builder quantity(int v) { this.quantity = v; return this; }
		public Builder unitPrice(String v) { this.unitPrice = new BigDecimal(v); return this; }
		public Builder customerName(String v) { this.customerName = v; return this; }
		public Builder purchasedAt(Instant v) { this.purchasedAt = v; return this; }

		public Order build() {
			return new Order(eventName, artist, eventDate, quantity, unitPrice, customerName, purchasedAt);
		}
	}
}
