package be.pxl.ticket;

import java.security.SecureRandom;

public final class TicketNumbers {

	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final SecureRandom RANDOM = new SecureRandom();

	private TicketNumbers() {
	}

	/** Format: GT-XXXXXXXX (8 characters from an alphabet without 0/O/1/I). */
	public static String next(TicketRequest request, java.util.Set<Bugs> bugs) {
		if (bugs.contains(Bugs.DUPES)) {
			// planted bug: seeded by the event name, so the same event always gets the same number
			return generate(new java.util.Random(request.eventName().hashCode()));
		}
		return generate(RANDOM);
	}

	private static String generate(java.util.Random random) {
		StringBuilder sb = new StringBuilder("GT-");
		for (int i = 0; i < 8; i++) {
			sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}
}
