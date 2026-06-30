# AI Tracker

An Android app that tracks the companies and stocks driving AI development — including
chip makers and AI hardware — alongside recent AI news.

## Features

- **Companies tab** — curated list of AI-relevant companies (NVIDIA, AMD, Broadcom, TSMC,
  ASML, Micron, Arm, Microsoft, Alphabet, Amazon, Meta, and more) with live price, daily
  change %, an inline sparkline, and category filters (Hardware, Semiconductors, Cloud,
  AI Models, Software).
- **Company detail** — current price, a 30-day price chart, a description, the company's
  specific AI/hardware focus, and company-specific news headlines.
- **News tab** — a live feed of recent AI-development headlines.
- **Watchlist** — star any company to track it in a personal watchlist (persisted on device).
- **Provider attribution** — each data screen shows which source supplied the data
  (e.g. "Prices via Yahoo Finance", "News via Google News").

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- MVVM with `ViewModel` + `StateFlow`
- Navigation Compose (bottom navigation + detail route)
- Retrofit + OkHttp + Gson
- Jetpack DataStore (watchlist persistence)
- In-memory TTL quote cache (`QuoteCache`) for price history
- Manual dependency injection (`AppContainer`)
- JUnit unit tests (`QuoteCacheTest`)

## Data sources

The app uses a swappable data-source abstraction (`StockDataSource` / `NewsDataSource`). It
defaults to free, no-key providers, and automatically upgrades to keyed providers when API
keys are configured.

| Domain | Default (no key) | Keyed provider |
|--------|------------------|----------------|
| Stocks | Yahoo Finance chart endpoint (`query1.finance.yahoo.com/v8/finance/chart/{symbol}`) | **Finnhub** (`finnhub.io/api/v1/quote`) |
| News   | Google News RSS (`news.google.com/rss/search`), parsed on-device | **NewsAPI** (`newsapi.org/v2/everything`) |

> The free endpoints are public and unofficial — no signup, but they may be rate-limited or
> change without notice. If prices fail to load, pull to refresh or try again later.

### Enabling keyed providers

Add your keys to `local.properties` (gitignored — keys are never committed or hardcoded):

```properties
FINNHUB_API_KEY=your_finnhub_key
NEWS_API_KEY=your_newsapi_key
```

They are exposed to the app as `BuildConfig.FINNHUB_API_KEY` / `BuildConfig.NEWS_API_KEY`
(also resolvable from a Gradle property or an environment variable of the same name). When a
key is present, `AppContainer` selects the keyed source; otherwise it falls back to the free
one. The active providers are logged at startup in debug builds.

> Note: Finnhub's free quote endpoint returns price + previous close but no intraday history,
> so the sparkline/30-day chart only renders with the Yahoo source.

## Getting started

1. **Open in Android Studio** (Hedgehog or newer). Use `File > Open` and select this folder.
   Android Studio will sync Gradle and **regenerate the Gradle wrapper JAR** automatically.
2. Let it create `local.properties` pointing at your Android SDK (or set `sdk.dir` yourself).
3. Run the `app` configuration on an emulator or device (min SDK 26 / Android 8.0).

### Building from the command line

The Gradle wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) is intentionally not committed
here. Generate it once with a local Gradle install:

```sh
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

(Or just open the project in Android Studio, which handles this for you.)

## Project structure

```
app/src/main/java/com/aitracker/app/
├── AiTrackerApp.kt          # Application + DI container bootstrap
├── AppContainer.kt          # Manual DI (Retrofit, OkHttp, repositories)
├── MainActivity.kt          # Compose entry point
├── data/
│   ├── model/               # Domain models + curated AI company catalog
│   ├── remote/              # Yahoo Finance API + Google News RSS service
│   │   ├── stock/           # StockDataSource: Yahoo (free) + Finnhub (keyed)
│   │   └── news/            # NewsDataSource: Google News (free) + NewsAPI (keyed)
│   ├── local/               # DataStore watchlist
│   ├── cache/               # In-memory TTL quote cache (price history)
│   └── repository/          # Stock / News / Watchlist repositories
└── ui/
    ├── theme/               # Material 3 theme
    ├── navigation/          # Bottom nav + NavHost
    ├── components/          # Reusable composables (rows, sparkline, chips)
    ├── stocks/              # Companies tab
    ├── detail/              # Company detail screen
    ├── news/                # News tab
    └── watchlist/           # Watchlist tab
```
