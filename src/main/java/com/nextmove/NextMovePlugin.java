package com.nextmove;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Next Move")
public class NextMovePlugin extends Plugin
{
	@Override
	protected void startUp()
	{
	}

	@Override
	protected void shutDown()
	{
	}

	@Provides
	NextMoveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NextMoveConfig.class);
	}
}
