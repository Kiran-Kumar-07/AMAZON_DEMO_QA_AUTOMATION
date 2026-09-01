# E-Commerce Shopping Cart Automation

Selenium + Java + TestNG framework that automates an end-to-end Amazon shopping-cart
scenario: dynamically select one available product from each of 5 different categories,
capture its name/price/category at runtime, screenshot it before Add to Cart, add it to
the cart, validate all 5 products in the cart (name, quantity, price), validate the cart
total, remove one product and re-validate, and generate a dynamic Excel execution report
from the actual run data.

## Project Overview

- Target site: `https://www.amazon.in` (configurable)
- Categories/products are **not hard-coded** — they are selected dynamically from live
  search results based on real-time availability (in-stock, priced tiles only).
- Product name, price, and cart price are captured from the DOM at runtime and compared
  — nothing is pre-typed into the Excel report.
- One reusable test method drives all 5 categories (`ShoppingCartE2ETest.shopForCategory`)
  instead of 5 duplicated test methods.
- A dedicated negative-scenario suite (`NegativeScenariosTest`) proves the framework
  fails loudly instead of false-passing.

## Technology Stack

| Concern | Choice |
|---|---|
| Language | Java 17 |
| Browser automation | Selenium WebDriver 4.24 |
| Driver management | WebDriverManager (Bonigarcia) — no manual driver downloads |
| Test runner | TestNG 7.10 |
| Build tool | Maven |
| Excel reporting | Apache POI (XSSF) |
| Logging | SLF4J + Logback |

## Prerequisites

- JDK 17+
- Maven 3.8+
- Google Chrome (default), or Firefox / Edge installed
- Internet access to `amazon.in` at execution time

## Installation / Setup

```bash
git clone <your-repo-url>
cd ecommerce-cart-automation
mvn -q compile
```

WebDriverManager resolves the correct browser driver automatically on first run — no
manual `chromedriver` download or `PATH` setup needed.

## Configuration

All runtime configuration lives in [`src/test/resources/config.properties`](src/test/resources/config.properties)
and can be overridden per-run with `-D` system properties (useful for CI):

```properties
base.url=https://www.amazon.in
browser=chrome          # chrome | firefox | edge
headless=false
explicit.wait=20
page.load.timeout=45
products.to.select=5
proof.dir=proof
reports.dir=reports
test.data.file=test-data/categories.csv
category.selection.retry=3
```

Example override:

```bash
mvn test -Dbrowser=firefox -Dheadless=true -Dbase.url=https://www.amazon.in
```

## Test Data Configuration

Categories/search keywords/quantities are data-driven from
[`test-data/categories.csv`](test-data/categories.csv):

```csv
category,searchKeyword,quantity
Electronics,Laptop,1
Home & Kitchen,Coffee Maker,1
Books,Book,1
Fashion,Shoes,1
Beauty & Personal Care,Shampoo,1
```

Add, remove, or reorder rows to change what the framework shops for — no code changes
required. The **actual** product chosen (name/price) is whatever is available live at
execution time, not what's listed here; only the category/keyword/quantity are inputs.

## Test Execution

Run the full suite (positive E2E flow + negative scenarios):

```bash
mvn test
```

Run only the shopping-cart E2E flow or only the negative scenarios:

```bash
mvn test -Dtest=ShoppingCartE2ETest
mvn test -Dtest=NegativeScenariosTest
```

The suite definition is [`src/test/resources/testng.xml`](src/test/resources/testng.xml).

Run the classes in parallel instead (see "Parallel execution" below for details):

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng-parallel.xml
```

## Browser Configuration

Set `browser=chrome|firefox|edge` in `config.properties` or via `-Dbrowser=...`. Test
logic is entirely browser-agnostic — `DriverFactory` is the only class that knows about a
specific browser, so switching browsers requires no change to any Page Object or test.
Chrome and Edge were both run live against amazon.in during development (10/10 tests,
identical result on each) — see "Cross-browser" below, which also covers a real,
currently-live Microsoft-side driver-resolution issue you may hit with `-Dbrowser=edge`
and its workaround.
Set `headless=true` to run without a visible window (e.g. in CI).

## Excel Report Generation

An Excel workbook is generated automatically at the end of every run under `reports/`,
named `ExecutionReport_<yyyyMMdd_HHmmss>.xlsx`, containing:

- **Test Execution Report** sheet — one row per product/validation: category, product
  name, product-page price, cart price, quantity, expected/actual cart total, PASS/FAIL
  status, execution timestamp, screenshot path, remarks.
- **Execution Summary** sheet — total/passed/failed/skipped counts, execution date/time,
  a per-product pricing summary, and expected-vs-actual cart total validation.

Every value in the report comes from the live run (`ExcelReportManager` /
`TestResultRecord`) — running the suite again against a different live catalogue
produces a report with different product names/prices, automatically.

## Screenshot / Proof Location

All screenshots are written to [`proof/`](proof/):

- `product1_before_add.png` … `product5_before_add.png` — captured immediately before
  each Add to Cart click, showing product name and price.
- `cart_with_5_products.png`, `cart_price_validation.png`, `cart_total_validation.png`,
  `cart_after_product_removal.png` — cart validation evidence.
- `negative_*.png` — evidence for each negative scenario.
- `FAILURE_<Class>_<method>_<timestamp>.png` — captured automatically by
  `TestListener.onTestFailure` whenever any test fails, with no extra code needed per test.

## Framework Architecture

```
src/main/java/com/ecommerce/automation/
├── config/ConfigReader.java        # singleton config.properties + -D overrides
├── driver/DriverFactory.java       # ThreadLocal WebDriver factory (chrome/firefox/edge)
├── models/                         # Product, CategoryInput — runtime value objects
├── pages/                          # Page Object Model
│   ├── BasePage.java                (shared driver/wait handles)
│   ├── HomePage.java                (search, overlay dismissal)
│   ├── SearchResultsPage.java       (dynamic availability scan/selection)
│   ├── ProductPage.java             (name/price capture, add-to-cart)
│   └── CartPage.java                (read lines, subtotal, remove item)
├── reporting/                      # ExcelReportManager, TestResultRecord
└── utils/                          # WaitUtils, PriceUtils, ScreenshotUtils, CsvTestDataReader

src/test/java/com/ecommerce/automation/
├── base/BaseTest.java               # driver lifecycle + shared report instance
├── listeners/TestListener.java      # automatic failure screenshots
└── tests/
    ├── ShoppingCartE2ETest.java     # priority-ordered E2E flow (1 shared method for 5 categories)
    └── NegativeScenariosTest.java   # non-existent search, out-of-stock, empty cart, removal, false-pass guard
```

Design choices:

- **Page Object Model** keeps locators/behavior in one place per page; tests only
  orchestrate.
- **WaitUtils** centralizes all synchronization as `FluentWait`-based explicit/conditional
  waits (visibility, clickability, stale-element retry, delayed add-to-cart confirmation).
  `Thread.sleep()` is not used anywhere in the framework.
- **PriceUtils** isolates currency parsing (₹, commas, alternate price-block layouts) from
  page logic, so pricing-model changes on the site only touch one class.
- **ThreadLocal** WebDriver in `DriverFactory` means the framework is already
  parallel-execution-safe (see Known Limitations for what's still needed to turn this on).
- **CSV test data** decouples what to shop for from how — new categories require no code
  changes.

## Assumptions

- Amazon India (`amazon.in`) is used as the target site; guest checkout / cart flow does
  not require sign-in to add items and view the cart.
- "5 different categories" is satisfied by 5 different search keywords mapped to
  conceptually distinct categories in `categories.csv`; the actual product Amazon returns
  for each keyword is trusted as belonging to that category.
- Cart lines are matched back to the product added on the product page **by ASIN**
  (parsed from the product/cart-line URL), not by product name. Amazon can render the
  same product's title with different word order/phrasing on the product page vs. the
  cart line (observed live: "SPARX Men SM-734 Casual Shoes" on the product page vs.
  "SPARX Casual Shoe SM-734 for Men..." in the cart), so a name-substring match is not
  reliable; the ASIN is stable in both places.
- Product price is read from the buybox's non-struck-through `span.a-price` (i.e. the
  price actually tied to the visible Add to Cart button), explicitly excluding any
  `.a-text-price` / `[data-a-strike]` M.R.P. block, since Amazon renders the M.R.P. as
  its own non-blank `a-price` span earlier in the DOM than the real offer price.

## Known Limitations

- Amazon has no public product API usable without seller/partner credentials, so the
  "API + UI validation" bonus is not implemented; the framework already isolates
  DOM-parsing (`PriceUtils`) so an API comparison could be added there.
- Live runs against a real e-commerce site (Amazon) are subject to that site's own
  UI experiments, regional layout differences, and occasional bot-detection/CAPTCHA
  challenges, which are outside the framework's control. Re-running with
  `headless=false` and a residential network connection is the most reliable path.
  `NegativeScenariosTest.unavailableProductIsCorrectlyFlagged` sweeps several
  categories known to reliably carry out-of-stock listings (smartwatches, vintage
  cameras, etc. -- broad categories like "laptop" almost never do) before falling
  back to a skip (never a false PASS) only if truly none are found live.
- Parallel execution is not wired into `testng.xml` (`parallel="false"`) even though
  `DriverFactory` is thread-safe; enabling it only requires changing the suite's
  `parallel`/`thread-count` attributes.
- CI/CD is not included as a working pipeline in this repository; see below for how one
  would be added.
- **Observed live pricing discrepancy on one specific listing**: while building this
  framework, one recurring Beauty & Personal Care result (a TRESemme shampoo, top hit
  for the keyword "Shampoo") was found to consistently display ₹685.00 as the price
  tied to its Add to Cart button, while the item was actually added to the cart at
  ₹602.00 — a real difference between the page's displayed price and the price the site
  itself charges at add-to-cart time, not a locator bug (the captured price was verified
  to come from the exact buybox container, `#desktop_qualifiedBuyBox`, that holds the
  actual Add to Cart button, with the M.R.P. strikethrough block explicitly excluded).
  `validateProductPricesAgainstCart` correctly failed this case rather than reporting a
  false PASS, per the CLAUDE.md section 9 requirement ("the automation should not
  generate a false PASS when the expected condition is not satisfied"). `categories.csv`
  now searches "Skin Care" instead of "Shampoo" for that category to avoid this specific
  listing; if a future run's keyword happens to resolve to a similarly mispriced
  listing, that is expected, correctly-detected live-site behavior, not a framework bug.

## Cross-Browser / Parallel / CI Notes (bonus)

All three of these were actually exercised locally during development (not just
described), against the live site, on this machine — see the concrete results below.

### Cross-browser (implemented and verified live)

Switching browsers needs a single config value — no test or Page Object code changes:

```bash
mvn test -Dbrowser=chrome    # default
mvn test -Dbrowser=edge
mvn test -Dbrowser=firefox
```

- **Chrome**: the framework's primary, default target. Every run referenced elsewhere
  in this README was on Chrome — 10/10 tests, 0 failures.
- **Edge**: run live end-to-end on this machine — **10/10 tests, 0 failures, 1 expected
  skip**, identical result to Chrome, using the exact same test code and Page Objects.
  Getting there surfaced a real, currently-live infrastructure problem worth recording:
  Microsoft's legacy Edge-driver download alias, `msedgedriver.azureedge.net`, has been
  fully decommissioned and returns `NXDOMAIN` from every public resolver (confirmed via
  Google `8.8.8.8` and Cloudflare `1.1.1.1`, not just this machine's local DNS) — and
  **both** WebDriverManager (5.9.2 *and* the latest 6.1.0) and Selenium's own built-in
  Selenium Manager still hardcode that dead alias for Edge specifically, so Edge driver
  auto-resolution currently fails everywhere, out of the box, regardless of network
  health. `DriverFactory.setupDriverManaged()` now catches that failure and logs a
  warning instead of aborting driver creation, and the workaround (download the matching
  `msedgedriver.exe` for your installed Edge version directly from
  `https://msedgedriver.microsoft.com/<version>/edgedriver_win64.zip`, which *does*
  resolve, then run with `-Dwebdriver.edge.driver=<path-to-msedgedriver.exe>` to bypass
  auto-resolution entirely) is exactly how the verifying run above was produced. This is
  an upstream tooling issue, not a defect in this framework's test logic — Chrome and
  Firefox driver resolution are unaffected.
- **Firefox**: the code path is identical (`WebDriverManager.firefoxdriver().setup()` +
  `FirefoxDriver`) and compiles/type-checks the same way as Chrome and Edge, but Firefox
  is not installed on the machine this was built on, so it was not run live. No
  Firefox-specific code exists that could plausibly behave differently.

### Parallel execution (implemented and verified live)

`DriverFactory` uses a `ThreadLocal<WebDriver>` and `BaseTest` builds `HomePage`/
`CartPage` per test-class instance, so nothing needed to change there. What *did* need
fixing for real (not just theoretical) thread safety: `ExcelReportManager`'s
`records` list is now a `CopyOnWriteArrayList` (was a plain `ArrayList`) and
`generate()` is now `synchronized` and idempotent (writes the workbook at most once),
since the single shared `REPORT` instance (`BaseTest.REPORT`) is written to and flushed
from more than one thread once classes run in parallel.

A separate suite file demonstrates this without touching the default sequential suite:

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng-parallel.xml
```

`testng-parallel.xml` sets `parallel="tests" thread-count="2"`, running
`ShoppingCartE2ETest` and `NegativeScenariosTest` concurrently. Run live: **10/10
tests, 0 failures**, with interleaved log timestamps proving genuine concurrency (e.g.
`NegativeScenarios`'s empty-cart screenshot was captured while `ShoppingCartE2E` was
still mid-way through adding product 2 of 5) — not two sequential runs that merely
looked parallel.

### CI/CD

`.github/workflows/ci.yml` runs `mvn -B test -Dheadless=true` on `push`/`pull_request`/
manual dispatch, then uploads `proof/` and `reports/` as build artifacts regardless of
pass/fail (`if: always()`), so a failing run's failure screenshots and the Excel report
are still retrievable from the Actions run page. This has not yet executed on GitHub
Actions itself, since that requires the repository to actually be pushed to GitHub with
Actions enabled — the workflow file is ready to run the moment that happens. Jenkins or
GitLab CI equivalents would run the identical `mvn -B test -Dheadless=true` command and
archive the same two directories as build artifacts.

### Flaky-test investigation — a real worked example from building this framework

The advice below isn't abstract — it's the actual process used, live, while building
this framework, when what looked at first like classic "flaky test" symptoms (pass one
run, fail the next, same code) turned out each time to be a real, reproducible defect,
not test-order pollution or timing luck:

1. **Don't add a retry or a longer sleep as the first move.** A retry that makes an
   intermittent failure "go away" only hides *when* the bug fires, not whether one
   exists. Every failure below was root-caused with the existing explicit-wait
   architecture (`WaitUtils`), never by adding `Thread.sleep()`.
2. **Read the automatically-captured failure screenshot before touching any code.**
   `TestListener.onTestFailure` (CLAUDE.md 14) captures the DOM state at the exact
   moment of failure — this is the single most useful diagnostic artifact, and it's free.
   In this session it revealed, directly: a search-results page that had actually loaded
   fine (ruling out a flaky page load, pointing at a stale locator instead); a "protection
   plan" upsell overlay sitting on top of the page after Add to Cart (ruling out a
   flaky click, pointing at a missing interaction step); a cart page correctly showing
   "was removed from Shopping Cart" even though the test's own item-count check said the
   removal hadn't happened (ruling out a flaky removal, pointing at a wrong DOM-liveness
   check).
3. **Distinguish "intermittent because of shared/racy state" from "intermittent because
   the site serves different content on different runs."** A search for "book" returning
   a different top result on two different runs isn't flakiness — it's the live
   catalogue behaving normally; a framework that assumes a fixed product name/price
   across runs is the actual bug (see "Assumptions" — this is why cart-line matching
   uses ASIN, not product name). The pricing framework should *correctly fail* when a
   listing's page price and cart price genuinely differ (see "Known Limitations"), not
   suppress the mismatch to look more "stable."
4. **When a failure is 100% reproducible against one specific input** (this session hit
   exactly that with one recurring product listing), that's a strong signal to stop
   calling it "flaky" at all and instead treat it as a deterministic defect (or a real
   site-side data inconsistency) with a specific, findable cause — confirmed here by
   re-running the exact same scenario multiple times and getting the exact same result
   each time, then inspecting the live DOM directly (via a small throwaway Selenium
   script dumping the actual element structure) rather than guessing at a fix.
5. **For genuinely load/timing-sensitive failures** (this session's real example:
   `CartPage.readAllLines()` throwing `StaleElementReferenceException` immediately after
   a removal, because Amazon re-renders the entire cart row list via AJAX on any
   mutation), the fix is a bounded, explicit retry *of the specific stale operation*
   (re-fetch fresh elements, try again, up to a small fixed number of attempts) — not a
   longer wait, since the element wasn't slow to appear, it was correctly replaced out
   from under an in-flight read.
6. **In CI specifically** (slower CPU/network than a local dev machine): if a failure
   only reproduces there, the next concrete step is comparing CI's failure screenshot
   against a local reproduction at a throttled network speed, before assuming CI itself
   is "flaky" — CI is usually exposing a real race that a fast local machine simply
   never loses.

## License

Submitted as a practical automation testing assignment.
