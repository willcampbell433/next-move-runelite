package com.nextmove;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("next-move")
public interface NextMoveConfig extends Config
{
	@ConfigItem(
		keyName = "publicLookupEnabled",
		name = "Enable public profile lookup",
		description = "Loads public Next Move analysis for a selected OSRS username.",
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers"
	)
	default boolean publicLookupEnabled()
	{
		return false;
	}
}
