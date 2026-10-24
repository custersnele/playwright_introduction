package be.pxl;
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.microsoft.playwright:playwright:1.55.0
 
/*
 * Bot-detection demo for students (Playwright for Java, single file).
 *
 * Opens each URL twice: once with a HEADLESS browser, once with a HEADED (visible) one,
 * and shows what the site "sees" and whether a bot check (Cloudflare, Akamai, DataDome, ...) kicked in.
 *
 * Run (easiest, with JBang - https://www.jbang.dev):
 *     jbang BotDetectionDemo.java
 *     jbang BotDetectionDemo.java https://www.delijn.be/nl/ https://bot.sannysoft.com/
 *
 * Or put this class in a Maven project with dependency com.microsoft.playwright:playwright.
 * The first run downloads the browsers automatically.
 *
 * Screenshots are written to ./screenshots/
 */
 
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
 
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

 
public class BotDetectionDemo {
 
    static final List<String> DEFAULT_URLS = List.of(
            "https://www.belgiantrain.be/nl",           // NMBS
            "https://www.delijn.be/nl/",                 // De Lijn
            "https://bot.sannysoft.com/",                // fingerprint test page: always shows the difference
            "https://www.browserscan.net/bot-detection"  // another test page
    );
 
    public static void main(String[] args) throws Exception {
        List<String> urls = args.length > 0 ? Arrays.asList(args) : DEFAULT_URLS;
        Path outDir = Paths.get("screenshots");
        Files.createDirectories(outDir);
 
        try (Playwright playwright = Playwright.create()) {
            for (boolean headless : new boolean[]{true, false}) {
                String mode = headless ? "HEADLESS" : "HEADED";
                System.out.println("\n==================== " + mode + " ====================");
 
                try (Browser browser = playwright.chromium()
                        .launch(new BrowserType.LaunchOptions().setHeadless(headless))) {
 
                    BrowserContext context = browser.newContext(
                            new Browser.NewContextOptions().setLocale("nl-BE").setViewportSize(1280, 800));
                    Page page = context.newPage();
 
                    for (String url : urls) {
                        check(page, url, mode, outDir);
                    }
                    context.close();
                }
            }
        }
    }
 
    static void check(Page page, String url, String mode, Path outDir) {
        System.out.println("\n--- " + url);
        try {
            Response resp = page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(30_000));
            page.waitForTimeout(5_000); // let JS challenges / redirects run
 
            Map<String, String> h = resp != null ? resp.headers() : Map.of();
            int status = resp != null ? resp.status() : -1;
            String title = page.title();
            String body = page.locator("body").innerText().toLowerCase();
 
            // What the browser reveals about itself
            Object webdriver = page.evaluate("() => navigator.webdriver");
            String ua = (String) page.evaluate("() => navigator.userAgent");
 
            // Which protection layer is in front of the site?
            String protection = detectProtection(h);
 
            // Signs that a challenge / block page was shown
            boolean cfChallenge = "challenge".equalsIgnoreCase(h.getOrDefault("cf-mitigated", ""))
                    || page.frames().stream().anyMatch(f -> f.url().contains("challenges.cloudflare.com"));
            boolean blockText = title.toLowerCase().matches(".*(just a moment|even geduld|attention required|access denied).*")
                    || body.contains("verify you are human")
                    || body.contains("verifieer dat u een mens bent")
                    || body.contains("access denied")
                    || body.contains("checking your browser");
            boolean blockedStatus = status == 403 || status == 429 || status == 503;
 
            System.out.println("  HTTP status        : " + status);
            System.out.println("  Page title         : " + title);
            System.out.println("  Protection / CDN   : " + protection);
            System.out.println("  navigator.webdriver: " + webdriver + "   <- true = automation is visible to the site");
            System.out.println("  User-Agent         : " + ua + (ua.contains("HeadlessChrome") ? "   <- says 'HeadlessChrome'!" : ""));
            System.out.println("  Bot check shown?   : "
                    + (cfChallenge || blockText || blockedStatus ? "YES ⚠️" : "no")
                    + (cfChallenge ? " (Cloudflare challenge)" : ""));
 
            String file = mode.toLowerCase() + "_" + url.replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9]+", "_") + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(outDir.resolve(file)));
            System.out.println("  Screenshot         : screenshots/" + file);
 
        } catch (PlaywrightException e) {
            // Some bot managers (e.g. Akamai) simply let the request hang or reset the connection
            System.out.println("  ❌ Failed: " + e.getMessage().lines().findFirst().orElse("")
                    + "   <- a timeout/reset can also be a silent block");
        }
    }
 
    static String detectProtection(Map<String, String> h) {
        String server = h.getOrDefault("server", "").toLowerCase();
        if (h.containsKey("cf-ray") || server.contains("cloudflare")) return "Cloudflare (cf-ray: " + h.getOrDefault("cf-ray", "-") + ")";
        if (server.contains("akamai") || h.containsKey("akamai-grn") || h.containsKey("x-akamai-transformed")) return "Akamai";
        if (h.containsKey("x-datadome") || h.containsKey("x-dd-b")) return "DataDome";
        if (h.containsKey("x-iinfo") || h.containsKey("x-cdn") && h.get("x-cdn").toLowerCase().contains("imperva")) return "Imperva";
        if (h.containsKey("x-amz-cf-id")) return "Amazon CloudFront";
        if (h.containsKey("x-azure-ref")) return "Azure Front Door";
        return server.isEmpty() ? "unknown" : "unknown (server: " + server + ")";
    }
}