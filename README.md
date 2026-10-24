## Playwright introduction

### SUT
You will be testing a web application for searching events and initiate a ticket purchase.
The web application is provided in the web directory.

```
docker compose -f deploy/docker-compose.yml up -d --build
```

This starts two containers: the web application (nginx) and the ticket service that creates the PDF tickets (`POST /api/tickets`, reached through nginx). You can open the web browser and navigate to http://localhost:5001/app/catalog.html to see one of the web pages. Stop everything with `docker compose -f deploy/docker-compose.yml down`.

### Codegen
Playwright comes with the ability to generate tests out of the box and is a great way to quickly get started with testing. It will open two windows, a browser window where you interact with the website you wish to test and the Playwright Inspector window where you can record your tests, copy the tests, clear your tests as well as change the language of your tests.

To run codegen execute the following command:
```
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen http://localhost:5001/app/catalog.html"
```

### Running the tests
The tests read their settings from system properties (defaults are in the `pom.xml` and `TestConfig`):

| Property | Default | Meaning |
|---|---|---|
| `app.url` | `http://localhost:5001/app/` | base URL of the web application (keep the trailing slash) |
| `headless` | `true` | set to `false` to watch the browser |
| `browser.type` | `chromium` | used by the best practices examples: `chromium`, `chrome`, `firefox`, `webkit` |

```
mvn test -Dapp.url=https://your-server/app/ -Dheadless=false
```

To host the web application on a server (Docker + Caddy), see `deploy/README.md`.

### Best practices and PDF tickets (exercise)
After checkout the basket page downloads a PDF ticket, made by the ticket service in `ticket-service/` (Java, Javalin, PDFBox). The slides `04. Best practices and testing PDF tickets.pptx` define the tasks.

`src/test/java/be/pxl/solution` holds a reference solution (remove it, or keep it on a separate branch, before you share the repository with students):

| Package | Content |
|---|---|
| `pages` | `BasePage` and the page objects that extend it; they return locators, assertions are in the tests |
| `fixtures` | `GlobotOptions` for `@UsePlaywright`, and a custom fixture `@WithOrder` |
| `data` | `Order` (builder), `OrderType`, `OrderFactory` |
| `pdf`, `api` | PDF helper (text, fonts, QR code, rendering), pixel comparison, client for the ticket service |
| `tests` | `TC2` fixtures, `TC3` ticket content (no browser), `TC4` PDF structure, QR and visual, `TC5` end-to-end |

The visual test compares with `src/test/resources/golden/ticket-standard.png`. Refresh it against a correct service with `mvn test -Dtest=TC4* -Dupdate.golden=true`.

### Instructor note: planted bugs in the ticket service
The environment variable `BUGS` of the ticket service switches the planted bugs on: `total` (cents are dropped from the total), `truncate` (event name cut at 30 characters), `date` (MM/dd/yyyy), `vat` (VAT line missing), `dupes` (same ticket number for the same event). Use `all` or a comma separated list; empty means a correct service.

```
BUGS=all docker compose -f deploy/docker-compose.yml up -d     # all bugs on
docker compose -f deploy/docker-compose.yml up -d              # back to a correct service
```

With `ALLOW_BUG_HEADER=true` a request can choose its bugs with the header `X-Bugs: vat,date` (for demos; keep it off during the bug hunt). Against all five bugs the reference tests TC3 and TC4 fail, the four end-to-end tests in TC5 find none: a good starting point for the testing pyramid discussion.

### Selenium vs Playwright
`src/test/java/be/pxl/comparison` contains the same scenario written with Selenium and with Playwright. Compare them side by side.

### Instructor note: planted bug
The filter in `web/app/js/main.js` lowercases the event name but not the search text, so searching for `Moon` finds nothing while `moon` works. `FilterEventsTest.filterIsCaseInsensitive` is `@Disabled` until the bug is fixed.

### Test automation and reporting
Run a subset with JUnit tags (see `Tags.java`: `smoke`, `ui`, `e2e`, `component`, `pdf`):

```
mvn test -Dgroups=smoke                    # only the smoke tests
mvn test -DexcludedGroups=e2e              # everything except the end-to-end tests
mvn test -Dgroups="component & pdf"        # tag expression
```

| Report | Command | Where |
|---|---|---|
| Surefire XML + text | `mvn test` | `target/surefire-reports/` |
| Surefire HTML | `mvn test surefire-report:report` | `target/site/surefire-report.html` |
| Allure | `mvn test` then `npx allure generate target/allure-results -o target/allure-report` and `npx allure open target/allure-report` | `target/allure-report/` (needs Node.js) |
| Playwright trace | `mvn test` (kept only for failed tests; `-Dtrace=on` keeps all) | `target/test-results/<test>/trace.zip` |

Open a trace with `mvn test-compile exec:java -D exec.classpathScope=test -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace target/test-results/<test>/trace.zip"`.

`mvn verify -Pci` is what the pipeline runs (`.github/workflows/tests.yml`): headless, and a failing test is retried once so a flaky test shows up as a flake instead of a failure. Start the workflow by hand from the Actions tab to choose the planted bugs (`all`) and the tags.
