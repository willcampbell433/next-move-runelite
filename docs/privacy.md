# Privacy

Next Move is a read-only visual companion. Public profile lookup is disabled by
default and displays RuneLite's required third-party connection warning before
it can be enabled.

## What is sent

When public lookup is enabled, the plugin sends:

- the selected OSRS username as a query parameter; and
- ordinary HTTPS request metadata, including the connecting IP address.

Requests go only to the fixed endpoint
`https://osrs-helper-six.vercel.app/api/runelite/profile`. The plugin does not
accept a custom server address and does not follow redirects.

## What the plugin reads

The response can contain:

- a public account score, tier, category scores, and titles;
- a bounded deck of public recommendations and their related Wiki pages;
- a boss-progression score, challenge, attempted kill counts, and achievements;
  and
- freshness and source labels used to explain the analysis.

The service builds this analysis from public OSRS Hiscores and, when available,
WikiSync quest state.

## What the plugin does not read or send

The plugin does not read or send login credentials, chat, friends lists,
screenshots, gameplay inputs, bank contents, inventory contents, equipped gear,
or arbitrary local files. It does not automate gameplay or write to the game.

## Local behavior

Only two preferences are stored through RuneLite configuration:

- whether public lookup is enabled; and
- the selected panel view.

Loaded profiles, friend lookups, and the selected recommendation for each
username remain in memory only. Disabling public lookup cancels active requests
and clears the loaded profile. Closing the plugin does the same.

## Server behavior

The endpoint is rate limited. As with any web service, hosting infrastructure
may retain standard operational request logs for reliability and abuse
prevention. Next Move does not receive a RuneScape or Jagex password.
