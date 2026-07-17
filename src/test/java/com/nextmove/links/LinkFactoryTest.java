package com.nextmove.links;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkFactoryTest
{
	@Test
	public void buildsFixedNextMoveLinks()
	{
		assertEquals(
			"https://osrs-helper-six.vercel.app/bosses?player=italiaboi69",
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
			"https://osrs-helper-six.vercel.app/runelite",
			LinkFactory.wikiSyncSetup());
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsInvalidUsernames()
	{
		LinkFactory.account("https://evil.invalid", LinkFactory.View.COACH);
	}
}
