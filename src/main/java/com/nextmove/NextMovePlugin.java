package com.nextmove;

import com.google.gson.Gson;
import com.nextmove.api.NextMoveClient;
import com.nextmove.completion.CompletionStore;
import com.nextmove.profile.ProfileController;
import com.nextmove.ui.NextMovePanel;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Next Move",
	description = "Shows quest-aware Next Move account scores, recommendations, and boss progression.",
	tags = {"account", "goals", "bosses", "recommendations", "hiscores", "quests"}
)
public class NextMovePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private NextMoveClient nextMoveClient;

	@Inject
	private Gson gson;

	private NextMovePanel panel;
	private ProfileController controller;
	private NavigationButton navigationButton;
	private NextMoveSession session;
	private QuestSnapshotBuilder questSnapshotBuilder;
	private CompletionStore completionStore;
	private boolean currentCharacterObserved;

	@Override
	protected void startUp()
	{
		panel = new NextMovePanel(configManager);
		completionStore = new CompletionStore(configManager, gson);
		controller = new ProfileController(nextMoveClient, panel, completionStore);
		panel.setController(controller, this::refreshActiveProfile);
		questSnapshotBuilder = new QuestSnapshotBuilder(NextMoveVersion.CURRENT);

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
				public void loadCurrentCharacter(String username, QuestSnapshot snapshot)
				{
					controller.setCurrentCharacter(username, snapshot);
				}

				@Override
				public void clearCurrentCharacter()
				{
					panel.setCurrentCharacterName(null);
					controller.clearCurrentCharacter();
				}

				@Override
				public void close()
				{
					controller.close();
				}
			});
		session.start();
		currentCharacterObserved = false;
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			scheduleInitialLocalPlayerLoad();
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
		questSnapshotBuilder = null;
		completionStore = null;
		currentCharacterObserved = false;
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
			currentCharacterObserved = loadLocalPlayer();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING
			|| event.getGameState() == GameState.CONNECTION_LOST)
		{
			currentCharacterObserved = false;
			session.loggedOut();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (session != null
			&& !currentCharacterObserved
			&& client.getGameState() == GameState.LOGGED_IN)
		{
			currentCharacterObserved = loadLocalPlayer();
		}
	}

	private boolean loadLocalPlayer()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null && localPlayer.getName() != null)
		{
			QuestSnapshot snapshot = questSnapshotBuilder.build(client, localPlayer.getName());
			session.loggedIn(localPlayer.getName(), snapshot);
			return true;
		}
		return false;
	}

	void scheduleInitialLocalPlayerLoad()
	{
		clientThread.invokeLater(() -> {
			if (session == null || questSnapshotBuilder == null
				|| client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			currentCharacterObserved = loadLocalPlayer();
		});
	}

	private void refreshActiveProfile()
	{
		if (controller == null)
		{
			return;
		}
		if (controller.isFriendActive())
		{
			controller.refresh();
			return;
		}
		clientThread.invokeLater(() -> {
			if (controller == null || questSnapshotBuilder == null
				|| client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			Player localPlayer = client.getLocalPlayer();
			if (localPlayer == null || localPlayer.getName() == null)
			{
				return;
			}
			QuestSnapshot snapshot = questSnapshotBuilder.build(
				client,
				localPlayer.getName());
			controller.refreshCurrentCharacter(snapshot);
		});
	}

}
