# Changelog

All notable changes to Next Move for RuneLite will be documented here.

## 0.2.1 — 2026-07-21

- Balanced the expanded recommendation response so boss suggestions cannot
  consume nearly every Coach slot.
- Added **Track this goal** to focus Coach on one recommendation for the
  current RuneLite session.
- Added **Browse other goals** to return to the complete filterable feed.

## 0.2.0 — 2026-07-21

- Replaced the one-at-a-time Coach carousel with a vertically scrollable feed
  of up to 32 ranked recommendations.
- Added All, Skilling, Bosses, Quests, PvM, and Unlock category filters.
- Expanded every recommendation inline with its rationale, evidence,
  checkpoint, Wiki guide, and Next Move website action.
- Kept older plugin releases compatible by requesting the expanded response
  only through the versioned 0.2 user agent.

## 0.1.3 — 2026-07-20

- Fixed re-enabling Next Move while already logged in by scheduling the initial
  quest snapshot on RuneLite's client thread.
- Added a regression test for the logged-in enable path.

## 0.1.2 — 2026-07-20

- Added a complete, first-party quest snapshot for the logged-in character.
- Combined transient RuneLite quest state with official Hiscores skills and KC.
- Recapture the full quest snapshot on explicit Refresh so missed events cannot
  leave the profile in a partially updated state.
- Kept friend lookup Hiscores-only and prevented the local character's quest
  data from being attached to friend requests.
- Added distinct source labels for quest-aware and Hiscores-only profiles.
- Documented the exact HTTPS payload, in-memory client behavior, and transient
  server processing boundary.

## 0.1.0 — 2026-07-18

- Added the read-only Account, Coach, and Bosses sidebar views.
- Added an explicit, disabled-by-default public profile lookup setting.
- Added current-character loading and temporary friend lookup.
- Added safe links to Next Move and full OSRS Wiki guides.
- Added bounded HTTPS responses, timeouts, cancellation, and strict response
  validation.
- Reworked the sidebar hierarchy for RuneLite's narrow panel width.
- Moved Account, Coach, and Bosses navigation above the page content.
- Made Settings a separate screen and collapsed player lookup after a profile
  loads.
- Fixed clipped copy, overflowing action rows, unsupported list glyphs, and
  false friend labels.
- Retry local-character detection after login until RuneLite exposes the name.
- Limit the Account trophy preview while keeping the full list on the website.
- Align every vertical sidebar section to the same left edge and normalize smart
  punctuation for RuneLite's display font.
- Rename the cramped Account tab to Power and present Account Power on a
  familiar 100-point scale.
- Add an explicit score-calculation link, center player lookup controls, wrap
  long status messages, and use public-facing Boss Bravery tier language.
- Point Account Power at the dedicated scoring rubric and translate unsupported
  recommendation arrows into RuneLite-safe text.
- Reserved additional sidebar width so long Coach copy wraps before the
  scrollbar instead of clipping its final characters.
- Made the player lookup action span the full sidebar width with clear spacing
  below the username field.
- Added a bounded Coach recommendation deck with **Next idea** cycling and a
  directly selectable **Other ideas** list.
- Isolated the selected recommendation per username and made the deck wrap
  instead of ending in an exhausted state.
- Moved the fixed production endpoint and browser links to `osrsnextmove.com`.
