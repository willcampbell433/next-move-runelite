# Next Move for RuneLite

[![CI](https://github.com/willcampbell433/next-move-runelite/actions/workflows/ci.yml/badge.svg)](https://github.com/willcampbell433/next-move-runelite/actions/workflows/ci.yml)

Next Move is a read-only RuneLite sidebar for answering the most important
question in Old School RuneScape: **what should I do next?**

It turns public account data into a playful account verdict, one focused
recommendation, and a boss-progression view without replacing the full Next
Move website.

Explore the full coach at [osrs-helper-six.vercel.app](https://osrs-helper-six.vercel.app).

## What v0.1 includes

- **Account** — overall account score, progression tier, category scores, and
  earned titles.
- **Coach** — one short-term recommendation with the reason it fits and the
  next meaningful unlock.
- **Bosses** — Boss Bravery score, next boss challenge, attempted kill counts,
  and boss achievements.
- **Friend lookup** — temporarily inspect another public OSRS account and
  return to the logged-in character with one click.
- **Safe links** — open the relevant full OSRS Wiki guide or the matching Next
  Move website page in the system browser.

Recommendation feedback such as **Done** and **Not today** remains on the
website for this first release.

## Setup

1. Install **Next Move** from the RuneLite Plugin Hub.
2. Open its sidebar panel.
3. Choose **Load profile** and accept RuneLite's third-party connection warning.

Skills and bosses work from public Hiscores. Enable **WikiSync** in RuneLite
for quest-aware recommendations. Next Move does not require an account or a
custom sync code for this visual companion.

The plugin starts with public lookup disabled. Turning it on sends the selected
OSRS username to `https://osrs-helper-six.vercel.app` over HTTPS. Disabling it
cancels active requests and clears the loaded profile from the panel.

## Privacy and data sources

Next Move reads analysis from a fixed, public, read-only endpoint. The analysis
is built from public OSRS Hiscores and, when available, WikiSync quest state.
The plugin does not automate gameplay, write to the game, or read credentials.
Friend lookups are temporary and are not saved by the plugin.

See [docs/privacy.md](docs/privacy.md) for the complete boundary.

## Development

This project follows RuneLite Plugin Hub conventions and targets Java 11.

```sh
JAVA_HOME=/path/to/jdk-11 ./gradlew clean test
JAVA_HOME=/path/to/jdk-11 ./gradlew run
```

The second command launches a developer RuneLite client. In-game verification
must be completed manually; see [docs/manual-qa.md](docs/manual-qa.md).

Every push and pull request also runs the complete test suite in GitHub Actions.

## Support and feedback

This is an early, free, open-source beta. Bug reports and focused feedback are
welcome in this repository's GitHub issues.

## License

[BSD 2-Clause](LICENSE)
