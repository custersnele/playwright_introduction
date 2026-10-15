package be.pxl.ticket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

import java.util.Map;
import java.util.Set;

public class Main {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static void main(String[] args) {
		int port = Integer.parseInt(env("PORT", "8080"));
		Set<Bugs> defaultBugs = Bugs.parse(System.getenv("BUGS"));
		boolean allowBugHeader = Boolean.parseBoolean(env("ALLOW_BUG_HEADER", "false"));

		Javalin app = Javalin.create(config -> config.showJavalinBanner = false);
		app.get("/api/health", ctx -> ctx.json(Map.of("status", "UP")));
		app.post("/api/tickets", ctx -> createTicket(ctx, defaultBugs, allowBugHeader));
		app.start(port);
		System.out.println("ticket-service started on port " + port + " (" + defaultBugs.size() + " planted bugs active)");
	}

	private static void createTicket(Context ctx, Set<Bugs> defaultBugs, boolean allowBugHeader) {
		TicketRequest request;
		try {
			request = MAPPER.readValue(ctx.bodyAsBytes(), TicketRequest.class);
		} catch (java.io.IOException | IllegalArgumentException e) {
			ctx.status(400).json(Map.of("error", "invalid JSON body"));
			return;
		}
		String error = request == null ? "request body is required" : request.validate();
		if (error != null) {
			ctx.status(400).json(Map.of("error", error));
			return;
		}
		Set<Bugs> bugs = defaultBugs;
		String header = ctx.header("X-Bugs");
		if (allowBugHeader && header != null) {
			bugs = Bugs.parse(header);
		}
		try {
			byte[] pdf = new TicketPdfGenerator(bugs).generate(request);
			ctx.contentType(ContentType.APPLICATION_PDF)
					.header("Content-Disposition", "attachment; filename=\"ticket.pdf\"")
					.result(pdf);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.status(500).json(Map.of("error", "could not generate ticket"));
		}
	}

	private static String env(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.isBlank() ? defaultValue : value;
	}
}
