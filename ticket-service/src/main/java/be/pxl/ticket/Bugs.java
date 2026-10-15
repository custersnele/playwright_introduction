package be.pxl.ticket;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Planted defects for the bug hunt. Switched on with the BUGS environment variable
 * (comma separated, e.g. BUGS=total,vat) or, when ALLOW_BUG_HEADER=true, per request with the X-Bugs header.
 */
public enum Bugs {
	/** Total is rounded down to whole dollars, so prices with cents give a wrong total. */
	TOTAL,
	/** Event names longer than 30 characters are cut off. */
	TRUNCATE,
	/** Purchase date printed as MM/dd/yyyy instead of dd/MM/yyyy. */
	DATE,
	/** The VAT line is missing. */
	VAT,
	/** Ticket number is derived from the event name, so two tickets for the same event share a number. */
	DUPES;

	public static Set<Bugs> parse(String csv) {
		Set<Bugs> result = EnumSet.noneOf(Bugs.class);
		if (csv == null || csv.isBlank()) {
			return result;
		}
		if (csv.trim().equalsIgnoreCase("all")) {
			return EnumSet.allOf(Bugs.class);
		}
		Arrays.stream(csv.split(","))
				.map(s -> s.trim().toUpperCase(Locale.ROOT))
				.filter(s -> !s.isEmpty())
				.forEach(s -> result.add(valueOf(s)));
		return result;
	}
}
