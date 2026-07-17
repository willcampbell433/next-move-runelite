package com.nextmove;

import com.google.inject.Provides;
import com.nextmove.api.NextMoveClient;
import com.nextmove.profile.ProfileController;
import com.nextmove.ui.NextMovePanel;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Next Move",
	description = "Shows public Next Move account scores, recommendations, and boss progression.",
	tags = {"account", "goals", "bosses", "recommendations", "hiscores", "wikisync"}
)
public class NextMovePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private NextMoveConfig config;

	@Inject
	private NextMoveClient nextMoveClient;

	private NextMovePanel panel;
	private ProfileController controller;
	private NavigationButton navigationButton;
	private NextMoveSession session;

	@Override
	protected void startUp()
	{
		panel = new NextMovePanel(configManager, config);
		controller = new ProfileController(nextMoveClient, panel);
		panel.setController(controller);

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel-icon.png");
		navigationButton = NavigationButton.builder()
			.tooltip("Next Move")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		session = new NextMoveSession(
			new NextMoveSession.ToolbarPort()
			{
				@Override
				public void add()
				{
					clientToolbar.addNavigation(navigationButton);
				}

				@Override
				public void remove()
				{
					clientToolbar.removeNavigation(navigationButton);
				}
			},
			new NextMoveSession.ProfilePort()
			{
				@Override
				public void showCurrentCharacter(String username)
				{
					panel.setCurrentCharacterName(username);
				}

				@Override
				public void loadCurrentCharacter(String username)
				{
					controller.setCurrentCharacter(username);
				}

				@Override
				public void clearCurrentCharacter()
				{
					panel.setCurrentCharacterName(null);
					controller.clearCurrentCharacter();
				}

				@Override
				public void clearProfile()
				{
					controller.clearProfile();
				}

				@Override
				public void close()
				{
					controller.close();
				}
			},
			config::publicLookupEnabled);
		session.start();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			loadLocalPlayer();
		}
	}

	@Override
	protected void shutDown()
	{
		if (session != null)
		{
			session.stop();
		}
		session = null;
		navigationButton = null;
		controller = null;
		panel = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (session == null)
		{
			return;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			loadLocalPlayer();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING
			|| event.getGameState() == GameState.CONNECTION_LOST)
		{
			session.loggedOut();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (session == null
			|| !"next-move".equals(event.getGroup())
			|| !"publicLookupEnabled".equals(event.getKey()))
		{
			return;
		}
		boolean enabled = config.publicLookupEnabled();
		panel.setLookupEnabledFromConfig(enabled);
		session.consentChanged(enabled);
	}

	private void loadLocalPlayer()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null && localPlayer.getName() != null)
		{
			session.loggedIn(localPlayer.getName());
		}
	}

	@Provides
	NextMoveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NextMoveConfig.class);
	}
}
