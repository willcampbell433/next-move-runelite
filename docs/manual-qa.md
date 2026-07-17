# Manual RuneLite QA

Complete these checks in the developer RuneLite client before requesting a
Plugin Hub review. Do not automate any interaction with the game.

1. Launch with Java 11 using `./gradlew run` and confirm RuneLite starts without
   a plugin exception.
2. Confirm the Next Move compass icon appears once in the right sidebar and the
   panel opens.
   The title should occupy its own row, with Refresh and Settings directly
   underneath it.
3. With public lookup disabled, confirm the panel shows the current character
   name but makes no request and displays **Load public profile**.
4. Choose **Load profile**, accept the warning, and confirm Account loads for
   the logged-in character.
5. Confirm Power displays the overall score out of 100, tier, five category
   scores, and titles without clipping at the normal RuneLite sidebar width.
   Power, Coach, and Bosses must remain visible above the content.
6. Open Coach and confirm exactly one recommendation, its rationale, unlock,
   and full Wiki guide link render correctly.
7. Open Bosses and confirm Boss Bravery, the next challenge, attempted bosses
   with positive KC, and achievements render correctly.
8. Open every external link and confirm it uses the system browser and stays on
   either `osrs-helper-six.vercel.app` or `oldschool.runescape.wiki`.
9. Look up `italiaboi69`, confirm the friend state is visually clear, and use
   **Return to my character**. The lookup field should stay collapsed until
   **Look up another player** is chosen.
10. Confirm `italiaboi69`'s Inferno rationale wraps without clipping words at
    the right edge.
11. Look up `no_noobs10` and confirm the recommendation reads **Sailing 15 to
    20** without a replacement glyph.
12. From Power, choose **How is this score calculated?** and confirm the
    player-independent Account Scoring page opens.
13. Refresh a loaded profile, hop worlds, and log out/in; confirm there are no
    duplicate sidebar buttons and the current account reloads only when needed.
14. Disable public lookup in Settings and confirm active work is cancelled, the
    profile clears, and the consent screen returns.
15. Stop or disable the plugin and confirm its sidebar button is removed with no
    errors in the developer console.
