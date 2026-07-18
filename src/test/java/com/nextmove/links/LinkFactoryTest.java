package com.nextmove.links;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkFactoryTest
{
	@Test
	public void buildsFixedNextMoveLinks()
	{
		assertEquals(
			"https://osrsnextmove.com/bosses?player=italiaboi69",
			LinkFactory.account("italiaboi69", LinkFactory.View.BOSSES));
	}

	@Test
	public void encodesWikiTitlesOnTheFixedOrigin()
	{
		assertEquals(
			"https://oldschool.runescape.wiki/w/TzHaar_Fight_Cave",
			LinkFactory.wiki("TzHaar Fight Cave"));
	}

	@Test
	public void exposesOnlyTheFixedSetupPage()
	{
		assertEquals(
			"https://osrsnextmove.com/runelite",
			LinkFactory.wikiSyncSetup());
	}

	@Test
	public void exposesAPlayerIndependentScoringGuide()
	{
		assertEquals(
			"https://osrsnextmove.com/scoring",
			LinkFactory.scoringGuide());
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsInvalidUsernames()
	{
		LinkFactory.account("https://evil.invalid", LinkFactory.View.COACH);
	}
}
