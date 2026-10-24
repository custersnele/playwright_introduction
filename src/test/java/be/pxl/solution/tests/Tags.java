package be.pxl.solution.tests;

/**
 * JUnit tag names, so a typo is a compile error instead of a test that silently never runs.
 * Select with Maven: mvn test -Dgroups=smoke   or   mvn test -DexcludedGroups=e2e
 */
public final class Tags {

	private Tags() {
	}

	/** A few fast tests that tell you whether the environment is alive at all. */
	public static final String SMOKE = "smoke";
	/** Browser tests of one page or feature. */
	public static final String UI = "ui";
	/** Full user journeys through browser, web app and ticket service. */
	public static final String E2E = "e2e";
	/** Tests of the ticket service alone: HTTP + PDF, no browser. */
	public static final String COMPONENT = "component";
	/** Tests that open or compare the PDF ticket. */
	public static final String PDF = "pdf";
}
