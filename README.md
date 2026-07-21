# Next Move for RuneLite

[![CI](https://github.com/willcampbell433/next-move-runelite/actions/workflows/ci.yml/badge.svg)](https://github.com/willcampbell433/next-move-runelite/actions/workflows/ci.yml)

Next Move is a read-only RuneLite sidebar for answering the most important
question in Old School RuneScape: **what should I do next?**

It combines official public Hiscores with the logged-in character's RuneLite
quest states to produce a playful account verdict, ranked short-term
recommendations, and a boss-progression view without replacing the full Next
Move website.

Explore the full coach at [osrsnextmove.com](https://osrsnextmove.com).

## What v0.2 includes

- **Account** — overall account score, progression tier, category scores, and
  earned titles.
- **Coach** — up to 50 ranked recommendations in one scrollable feed, with
  Skilling, Bosses, Quests, PvM, and Unlock filters. Every idea includes the
  reason it fits and the next meaningful checkpoint. Choose **Track this goal**
  to temporarily focus the sidebar on one idea while you play.
- **Bosses** — Boss Bravery score, next boss challenge, attempted kill counts,
  and boss achievements.
- **Friend lookup** — temporarily inspect another public OSRS account and
  return to the logged-in character with one click.
- **Safe links** — open the relevant full OSRS Wiki guide or the matching Next
  Move website page in the system browser.

The Coach preserves the strongest recommendation while balancing the expanded
feed across the available activity types. A focused goal lasts for the current
RuneLite session. Durable **Done**, saved goals, and recommendation history
remain on the website.

## Setup

Next Move is currently awaiting RuneLite Plugin Hub review. After approval:

1. Install **Next Move** from the RuneLite Plugin Hub.
2. Open its sidebar panel.
3. Accept RuneLite's third-party connection warning when prompted.

The logged-in character loads automatically. Its skills, activities, and boss
kill counts come from official public OSRS Hiscores, while its complete quest
state is read directly from the local RuneLite client. You can also look up a
friend's public profile by username, but friend lookups remain Hiscores-only and
never guess quest completion. Next Move does not require an account or a custom
sync code for this visual companion.

## Privacy and data sources

Next Move sends the logged-in character's RSN and complete quest states to its
fixed HTTPS endpoint. The service processes that snapshot transiently, combines
it with official public OSRS Hiscores, and does not persist the snapshot. The
plugin does not automate gameplay, write to the game, or read credentials.
Friend lookups send only the selected public RSN and remain in memory.

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
welcome in this repository's GitHub issues. See [CONTRIBUTING.md](CONTRIBUTING.md)
before opening a pull request. Please report security concerns privately as
described in [SECURITY.md](SECURITY.md).

## License

[BSD 2-Clause](LICENSE)
