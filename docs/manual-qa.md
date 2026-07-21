# Manual RuneLite QA

Complete these checks in the developer RuneLite client before requesting a
Plugin Hub review. Do not automate any interaction with the game.

1. Launch with Java 11 using `./gradlew run` and confirm RuneLite starts without
   a plugin exception.
2. Confirm the Next Move compass icon appears once in the right sidebar and the
   panel opens.
   The title should occupy its own row, with Refresh and Settings directly
   underneath it.
3. Log in and confirm Account loads automatically for the current character
   after RuneLite's third-party connection warning has been accepted. The
   status must read **RuneLite quests · Hiscores skills**.
4. Open Settings and confirm it identifies the fixed `osrsnextmove.com` HTTPS
   service, official public Hiscores, and the logged-in character's quest-state
   upload. It must also explain that friend lookups remain Hiscores-only.
5. Confirm Power displays the overall score out of 100, tier, five category
   scores, and titles without clipping at the normal RuneLite sidebar width.
   Power, Coach, and Bosses must remain visible above the content.
6. Open Coach and confirm the primary recommendation, its rationale, unlock,
   and full Wiki guide link render correctly. Confirm no available category
   consumes more than seven slots. Choose **Track this goal** and confirm Coach
   shows only that recommendation. Switch to Power and back to Coach, confirm
   the focus remains, then choose **Browse other goals** to restore the feed.
7. Open Bosses and confirm Boss Bravery, the next challenge, attempted bosses
   with positive KC, and achievements render correctly.
8. Open every external link and confirm it uses the system browser and stays on
   either `osrsnextmove.com` or `oldschool.runescape.wiki`.
9. Look up `italiaboi69`, confirm the friend state is visually clear and reads
   **Hiscores only · Quests unavailable**, and use **Return to my character**.
   Confirm the returned character restores **RuneLite quests · Hiscores
   skills**. The lookup field should stay collapsed until
   **Look up another player** is chosen. When open, **Look up player** should
   span the same full width as the username field with a visible gap between
   them.
10. Confirm `italiaboi69`'s Inferno rationale wraps without clipping words at
    the right edge.
11. Look up `no_noobs10` and confirm the recommendation reads **Sailing 15 to
    20** without a replacement glyph.
12. From Power, choose **How is this score calculated?** and confirm the
    player-independent Account Scoring page opens.
13. Change a visible quest state if practical, choose **Refresh**, and confirm
    the recommendation updates from a newly captured full snapshot. Then hop
    worlds and log out/in; confirm there are no duplicate sidebar buttons and
    the current account reloads only when needed.
14. Stop or disable the plugin and confirm its sidebar button is removed with no
    errors in the developer console.
