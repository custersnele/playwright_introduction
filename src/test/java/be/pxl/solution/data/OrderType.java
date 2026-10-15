package be.pxl.solution.data;

/** Every value is a deliberately chosen test case: ask yourself which risk it covers. */
public enum OrderType {
	/** The happy path. */
	STANDARD,
	/** High price: wide numbers, thousands. */
	VIP,
	/** Several tickets: the maximum the UI offers (4). */
	GROUP,
	/** Event name of exactly 80 characters, the maximum: wrapping and truncation. */
	LONG_NAME,
	/** é, ë, ü, Ł, ★, ♪: does the font contain them and does extraction return them unchanged? */
	UNICODE_NAME,
	/** Unit price with cents: 3 x 63.33 = 189.99, catches rounding errors. */
	CENTS_PRICE,
	/** The maximum quantity the service accepts (10). */
	MAX_QUANTITY
}
