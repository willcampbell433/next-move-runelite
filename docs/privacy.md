# Privacy

Next Move is a read-only visual companion. It requires its fixed third-party
service to turn a public OSRS username into the profile shown in its sidebar.
RuneLite displays the Plugin Hub third-party connection warning before use.

## What is sent

When the logged-in character or a friend is selected, the plugin sends:

- the selected OSRS username as a query parameter; and
- ordinary HTTPS request metadata, including the connecting IP address.

For the currently logged-in character only, it also sends:

- the display name read from the local player;
- every RuneLite quest enum key and display name;
- each quest's `NOT_STARTED`, `IN_PROGRESS`, or `FINISHED` state; and
- the snapshot time and plugin version; and
- up to 200 recommendation IDs that this RuneLite installation has marked
  complete for the selected character.

Friend lookup never includes the logged-in character's quest snapshot.

Requests go only to the fixed endpoint
`https://osrsnextmove.com/api/runelite/profile`. The plugin does not
accept a custom server address and does not follow redirects.

## What the plugin reads

The response can contain:

- a public account score, tier, category scores, and titles;
- a bounded deck of public recommendations and their related Wiki pages;
- a boss-progression score, challenge, attempted kill counts, and achievements;
  and
- freshness and source labels used to explain the analysis.

The service combines official public OSRS Hiscores with the logged-in
character's submitted quest snapshot. A friend profile remains Hiscores-only,
so its quest completion is unavailable and is never guessed.

## What the plugin does not read or send

The plugin does not read or send login credentials, chat, friends lists,
screenshots, gameplay inputs, bank contents, inventory contents, equipped gear,
or arbitrary local files. It does not automate gameplay or write to the game.

## Local behavior

The selected panel view and an account-scoped ledger of completed
recommendations are stored through RuneLite configuration. Each completion
contains the recommendation ID, title, category, and completion time. The
ledger is bounded to 200 entries per character and can be changed only while
viewing the logged-in character.

Loaded profiles, quest snapshots, friend lookups, and the selected focused goal
for each username remain in memory only. Closing the plugin cancels active
requests and clears that in-memory state.

## Server behavior

The endpoint validates and processes quest snapshots and completion IDs only to
create the current response; it does not write either to Next Move's database.
The endpoint is rate limited. As with any web service, hosting infrastructure
may retain standard operational request logs for reliability and abuse
prevention. Next Move does not receive a RuneScape or Jagex password.
