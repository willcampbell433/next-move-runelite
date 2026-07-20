package com.nextmove.links;

import java.util.Objects;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;

public final class LinkFactory
{
	private static final HttpUrl NEXT_MOVE = Objects.requireNonNull(
		HttpUrl.parse("https://osrsnextmove.com"));
	private static final HttpUrl WIKI = Objects.requireNonNull(
		HttpUrl.parse("https://oldschool.runescape.wiki"));
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9 _-]{1,12}");

	private LinkFactory()
	{
	}

	public enum View
	{
		COACH("coach"),
		BOSSES("bosses"),
		STATS("stats");

		private final String path;

		View(String path)
		{
			this.path = path;
		}
	}

	public static String account(String username, View view)
	{
		String validated = username == null ? "" : username.trim();
		if (!USERNAME.matcher(validated).matches())
		{
			throw new IllegalArgumentException("Invalid OSRS username");
		}
		return NEXT_MOVE.newBuilder()
			.addPathSegment(Objects.requireNonNull(view).path)
			.addQueryParameter("player", validated)
			.build()
			.toString();
	}

	public static String wiki(String wikiTitle)
	{
		String title = wikiTitle == null ? "" : wikiTitle.trim();
		if (title.isEmpty() || title.length() > 120)
		{
			throw new IllegalArgumentException("Invalid Wiki title");
		}
		return WIKI.newBuilder()
			.addPathSegment("w")
			.addPathSegment(title.replace(' ', '_'))
			.build()
			.toString();
	}

	public static String runeliteGuide()
	{
		return NEXT_MOVE.newBuilder()
			.addPathSegment("runelite")
			.build()
			.toString();
	}

	public static String scoringGuide()
	{
		return NEXT_MOVE.newBuilder()
			.addPathSegment("scoring")
			.build()
			.toString();
	}
}
