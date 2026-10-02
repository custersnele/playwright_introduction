## Playwright introduction

### SUT
You will be testing a web application for searching events and initiate a ticket purchase.
The web application is provided in the web directory.

```
cd web
docker build -t playwright-intro .
docker run -d --name playwright-intro -p 5001:80 playwright-intro
```

You can open the web browser and navigate to http://localhost:5001/app/catalog.html to see one of the web pages.

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

### Selenium vs Playwright
`src/test/java/be/pxl/comparison` contains the same scenario written with Selenium and with Playwright. Compare them side by side.

### Instructor note: planted bug
The filter in `web/app/js/main.js` lowercases the event name but not the search text, so searching for `Moon` finds nothing while `moon` works. `FilterEventsTest.filterIsCaseInsensitive` is `@Disabled` until the bug is fixed.
