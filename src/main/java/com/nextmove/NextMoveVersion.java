package com.nextmove;

public final class NextMoveVersion
{
	public static final String CURRENT = "0.3.0";

	public static String userAgent()
	{
		return "Next-Move-RuneLite/" + CURRENT;
	}

	private NextMoveVersion()
	{
	}
}
