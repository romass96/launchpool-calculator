# Launchpool Calculator

A web application for calculating the time-weighted average balance over crypto launchpool staking periods. Useful for estimating average holdings when participating in exchange launchpools (e.g. Binance Launchpool, Bybit Launchpool) where rewards are based on your average balance during the staking period.

## Features

- **Average Balance Calculator** — Define a launchpool period (start and end dates), add your deposit/withdrawal transactions, and get the time-weighted average balance in USD
- **Coin Data** — Supports 1500+ cryptocurrencies sourced from [CoinGecko API](https://www.coingecko.com/en/api)
- **Historical Prices** — Fetches historical prices to value your holdings in USD at each hour of the staking period
- **Persistent Storage** — Available coins are cached in an H2 database and synced from CoinGecko every 10 minutes

## Tech Stack

- **Java 21**
- **Spring Boot 4** — Web, Data JPA, Scheduling
- **Vaadin 25** — Web UI
- **H2** — Embedded database (file-based persistence)
- **CoinGecko API** — Cryptocurrency data and historical prices

## Prerequisites

- Java 21 or later
- [CoinGecko API key](https://www.coingecko.com/en/api) (Pro or Demo plan for higher rate limits)

## Getting Started

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

The application will start at [http://localhost:8080](http://localhost:8080).

### Configuration

Create or edit `src/main/resources/application.yml` (or use environment variables):

```yaml
integrations:
  coingecko:
    api-key: "your-coingecko-api-key"
    api-url: "https://api.coingecko.com/api/v3"
```

The CoinGecko API key is sent as the `x_cg_pro_api_key` header. The free tier has rate limits; the app handles 429 responses with retry logic using the `Retry-After` header.

### Database

Coin data is stored in `./data/launchpool` (H2 file database). The `data/` directory is created automatically and is gitignored.

## Project Structure

```
src/main/java/io/dobermoney/launchpool/
├── LaunchpoolServiceApplication.java
├── calculator/          # Average balance calculation logic
├── client/              # CoinGecko API HTTP client
├── component/           # Vaadin UI components
├── config/              # Retry config, Coingecko properties
├── entity/              # JPA entities
├── model/               # Domain models
├── repository/          # Spring Data JPA repositories
├── scheduler/           # Coin sync from CoinGecko
├── service/             # Coin service (DB + API implementations)
└── view/                # Vaadin views
```

## Usage

1. Open **Average balance calculator** from the sidebar
2. Select **Launchpool Start** and **Launchpool End** dates
3. Click **Add Transaction** to record deposits and withdrawals
4. For each transaction: choose date/time, type (Deposit/Withdraw), coin, and amount
5. Click **Calculate** to get your time-weighted average balance in USD
