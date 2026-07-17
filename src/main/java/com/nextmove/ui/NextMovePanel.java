package com.nextmove.ui;

import com.nextmove.NextMoveConfig;
import com.nextmove.links.LinkFactory;
import com.nextmove.profile.ProfileController;
import com.nextmove.profile.ProfileState;
import com.nextmove.profile.ProfileView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

public class NextMovePanel extends PluginPanel implements ProfileView
{
	private static final String CONFIG_GROUP = "next-move";
	private static final String LOOKUP_KEY = "publicLookupEnabled";
	private static final String VIEW_KEY = "selectedView";
	private static final String HOST = "osrs-helper-six.vercel.app";
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9 _-]{1,12}");

	interface Settings
	{
		boolean lookupEnabled();

		void setLookupEnabled(boolean enabled);

		String selectedView();

		void setSelectedView(String selectedView);
	}

	interface Actions
	{
		void load(String username, boolean friend);

		void setCurrentCharacter(String username);

		void refresh();

		void returnToCurrentCharacter();

		void clearProfile();
	}

	private enum View
	{
		ACCOUNT("Power"),
		COACH("Coach"),
		BOSSES("Bosses");

		private final String label;

		View(String label)
		{
			this.label = label;
		}
	}

	private final Settings settings;
	private Actions actions;
	private ProfileState state = ProfileState.notLoaded(null);
	private String currentCharacterName;
	private boolean lookupEnabled;
	private boolean settingsOpen;
	private boolean playerLookupOpen;
	private View selectedView;

	public NextMovePanel(ConfigManager configManager, NextMoveConfig config)
	{
		this(new RuneLiteSettings(configManager, config), new NoOpActions());
	}

	NextMovePanel(Settings settings, Actions actions)
	{
		this.settings = Objects.requireNonNull(settings);
		this.actions = Objects.requireNonNull(actions);
		lookupEnabled = settings.lookupEnabled();
		selectedView = parseView(settings.selectedView());
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		rebuild();
	}

	public void setController(ProfileController controller)
	{
		Objects.requireNonNull(controller);
		actions = new Actions()
		{
			@Override
			public void load(String username, boolean friend)
			{
				controller.load(username, friend);
			}

			@Override
			public void setCurrentCharacter(String username)
			{
				controller.setCurrentCharacter(username);
			}

			@Override
			public void refresh()
			{
				controller.refresh();
			}

			@Override
			public void returnToCurrentCharacter()
			{
				controller.returnToCurrentCharacter();
			}

			@Override
			public void clearProfile()
			{
				controller.clearProfile();
			}
		};
	}

	public void setCurrentCharacterName(String username)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> setCurrentCharacterName(username));
			return;
		}
		currentCharacterName = username == null || username.trim().isEmpty()
			? null
			: username.trim();
		state = state.withCurrentCharacter(currentCharacterName);
		rebuild();
	}

	public void setLookupEnabledFromConfig(boolean enabled)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> setLookupEnabledFromConfig(enabled));
			return;
		}
		lookupEnabled = enabled;
		if (!enabled)
		{
			state = ProfileState.notLoaded(currentCharacterName);
		}
		rebuild();
	}

	@Override
	public void render(ProfileState next)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> render(next));
			return;
		}
		state = Objects.requireNonNull(next);
		if (next.getCurrentCharacterUsername() != null)
		{
			currentCharacterName = next.getCurrentCharacterUsername();
		}
		rebuild();
	}

	private void rebuild()
	{
		removeAll();
		add(new PanelHeader(actions::refresh, () -> {
			settingsOpen = !settingsOpen;
			playerLookupOpen = false;
			rebuild();
		}), BorderLayout.NORTH);

		JPanel body = vertical();
		body.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		body.add(new JLabel(activeAccountLabel()));
		body.add(Box.createRigidArea(new Dimension(0, 4)));
		body.add(new StatusPanel(statusText()));
		body.add(Box.createRigidArea(new Dimension(0, 10)));

		if (settingsOpen)
		{
			body.add(settingsPanel());
		}
		else if (!lookupEnabled)
		{
			body.add(consentPanel());
		}
		else
		{
			body.add(navigationPanel());
			body.add(Box.createRigidArea(new Dimension(0, 12)));
			body.add(contentPanel());
			body.add(Box.createRigidArea(new Dimension(0, 12)));
			body.add(playerToolsPanel());
		}

		add(body, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private JPanel consentPanel()
	{
		JPanel panel = vertical();
		String character = currentCharacterName == null ? "a public character" : currentCharacterName;
		panel.add(new JLabel("Load public profile"));
		panel.add(Box.createRigidArea(new Dimension(0, 6)));
		panel.add(wrapped("Load the public Next Move profile for " + character + "?"));
		panel.add(wrapped(
			"Sends only this public character name to " + HOST
				+ " and receives analysis built from Hiscores and WikiSync."));
		JButton accept = new JButton("Load profile");
		accept.setAlignmentX(Component.LEFT_ALIGNMENT);
		accept.addActionListener(event -> {
			lookupEnabled = true;
			settings.setLookupEnabled(true);
			if (currentCharacterName != null)
			{
				actions.setCurrentCharacter(currentCharacterName);
			}
			settingsOpen = false;
			rebuild();
		});
		panel.add(Box.createRigidArea(new Dimension(0, 8)));
		panel.add(accept);
		return panel;
	}

	private JPanel contentPanel()
	{
		ProfileState.Status status = state.getStatus();
		if (status == ProfileState.Status.LOADING)
		{
			return messagePanel("Loading " + safeUsername(state.getActiveUsername()) + "…");
		}
		if (status == ProfileState.Status.NOT_FOUND)
		{
			return retryPanel("Player not found: " + safeUsername(state.getActiveUsername()));
		}
		if (status == ProfileState.Status.RATE_LIMITED)
		{
			String wait = state.getRetryAfterSeconds() == null
				? "Try again shortly."
				: "Try again in " + state.getRetryAfterSeconds() + " seconds.";
			return retryPanel("Public lookups are rate limited. " + wait);
		}
		if (status == ProfileState.Status.UNAVAILABLE)
		{
			return retryPanel("Next Move is temporarily unavailable.");
		}
		if (status == ProfileState.Status.INCOMPATIBLE_RESPONSE)
		{
			return retryPanel("Next Move needs an update.");
		}
		if (status == ProfileState.Status.LOADED || status == ProfileState.Status.LOADED_STALE)
		{
			JPanel panel = vertical();
			if (status == ProfileState.Status.LOADED_STALE)
			{
				panel.add(wrapped("Showing the last in-memory result. Refresh failed."));
				panel.add(Box.createRigidArea(new Dimension(0, 6)));
			}
			if (selectedView == View.ACCOUNT)
			{
				AccountPanel account = new AccountPanel();
				account.render(state.getProfile().getProfile());
				panel.add(account);
			}
			else if (selectedView == View.COACH)
			{
				CoachPanel coach = new CoachPanel();
				coach.render(state.getProfile().getProfile());
				panel.add(coach);
			}
			else
			{
				BossesPanel bosses = new BossesPanel();
				bosses.render(state.getProfile().getProfile());
				panel.add(bosses);
			}
			return panel;
		}
		return messagePanel(currentCharacterName == null
			? "Log in or look up a friend to load a public profile."
			: "Load " + currentCharacterName + " when you are ready.");
	}

	private JPanel retryPanel(String message)
	{
		JPanel panel = messagePanel(message);
		JButton retry = new JButton("Retry");
		retry.addActionListener(event -> actions.refresh());
		JButton website = new JButton("Open Next Move");
		website.addActionListener(event -> LinkBrowser.browse(websiteLink()));
		panel.add(SidebarUi.buttonStack(retry, website));
		return panel;
	}

	private JPanel playerToolsPanel()
	{
		JPanel panel = vertical();
		boolean loaded = state.getStatus() == ProfileState.Status.LOADED
			|| state.getStatus() == ProfileState.Status.LOADED_STALE;
		if (!loaded || playerLookupOpen)
		{
			panel.add(friendLookupPanel());
		}
		else
		{
			JButton openLookup = new JButton("Look up another player");
			openLookup.setAlignmentX(Component.LEFT_ALIGNMENT);
			openLookup.setMaximumSize(new Dimension(
				Integer.MAX_VALUE,
				openLookup.getPreferredSize().height));
			openLookup.addActionListener(event -> {
				playerLookupOpen = true;
				rebuild();
			});
			panel.add(openLookup);
		}

		if (state.isFriendActive() && currentCharacterName != null)
		{
			JButton back = new JButton("Return to my character");
			back.addActionListener(event -> {
				playerLookupOpen = false;
				actions.returnToCurrentCharacter();
			});
			panel.add(Box.createRigidArea(new Dimension(0, 5)));
			panel.add(SidebarUi.buttonStack(back));
		}
		return panel;
	}

	private JPanel friendLookupPanel()
	{
		JPanel panel = vertical();
		JLabel label = new JLabel("Look up friend", JLabel.CENTER);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
		panel.add(label);
		JTextField username = new JTextField();
		username.setAlignmentX(Component.LEFT_ALIGNMENT);
		username.setMaximumSize(new Dimension(Integer.MAX_VALUE, username.getPreferredSize().height));
		panel.add(username);
		JButton lookup = new JButton("Look up player");
		lookup.addActionListener(event -> {
			String selected = username.getText().trim();
			if (USERNAME.matcher(selected).matches())
			{
				playerLookupOpen = false;
				actions.load(selected, true);
			}
		});
		panel.add(Box.createRigidArea(new Dimension(0, 7)));
		panel.add(SidebarUi.buttonStack(lookup));
		return panel;
	}

	private JPanel navigationPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1, View.values().length, 4, 0));
		panel.setOpaque(false);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		for (View view : View.values())
		{
			JButton button = new JButton(view.label);
			button.setEnabled(view != selectedView);
			button.addActionListener(event -> {
				selectedView = view;
				settingsOpen = false;
				playerLookupOpen = false;
				settings.setSelectedView(view.name());
				rebuild();
			});
			panel.add(button);
		}
		return panel;
	}

	private JPanel settingsPanel()
	{
		JPanel panel = vertical();
		JLabel title = new JLabel("PRIVACY");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(title);
		panel.add(Box.createRigidArea(new Dimension(0, 6)));
		panel.add(wrapped("Public lookup uses Next Move's fixed HTTPS service."));
		panel.add(wrapped("Skills and bosses come from public Hiscores. Quest-aware advice uses WikiSync when available."));
		if (lookupEnabled)
		{
			JButton disable = new JButton("Disable public lookup");
			disable.setAlignmentX(Component.LEFT_ALIGNMENT);
			disable.addActionListener(event -> {
				lookupEnabled = false;
				settings.setLookupEnabled(false);
				actions.clearProfile();
				state = ProfileState.notLoaded(currentCharacterName);
				settingsOpen = false;
				rebuild();
			});
			panel.add(Box.createRigidArea(new Dimension(0, 8)));
			panel.add(SidebarUi.buttonStack(disable));
		}
		JButton close = new JButton("Back to profile");
		close.addActionListener(event -> {
			settingsOpen = false;
			rebuild();
		});
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(SidebarUi.buttonStack(close));
		return panel;
	}

	private String activeAccountLabel()
	{
		String username = state.getActiveUsername() != null
			? state.getActiveUsername()
			: currentCharacterName;
		if (username == null)
		{
			return "No character selected";
		}
		if (state.isFriendActive())
		{
			return username + " · Friend";
		}
		if (currentCharacterName != null && username.equalsIgnoreCase(currentCharacterName))
		{
			return username + " · My character";
		}
		return username + " · Public profile";
	}

	private String statusText()
	{
		if (!lookupEnabled)
		{
			return "Public lookup off";
		}
		if (state.getStatus() == ProfileState.Status.LOADED_STALE)
		{
			return "Stale · refresh failed";
		}
		if (state.getProfile() != null && state.getProfile().getProfile() != null)
		{
			return "WIKISYNC".equals(state.getProfile().getProfile().getDataSource())
				? "WikiSync ready"
				: "Hiscores only · Quests unavailable";
		}
		return state.getMessage();
	}

	private String websiteLink()
	{
		String username = state.getActiveUsername() != null
			? state.getActiveUsername()
			: currentCharacterName;
		if (username == null)
		{
			return LinkFactory.wikiSyncSetup();
		}
		return LinkFactory.account(username, LinkFactory.View.COACH);
	}

	private static String safeUsername(String username)
	{
		return username == null || username.isEmpty() ? "player" : username;
	}

	private static JPanel messagePanel(String message)
	{
		JPanel panel = vertical();
		panel.add(wrapped(message));
		return panel;
	}

	private static JPanel vertical()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		return panel;
	}

	private static JLabel wrapped(String text)
	{
		return SidebarUi.wrapped(text);
	}

	private static View parseView(String stored)
	{
		try
		{
			return View.valueOf(stored == null ? "ACCOUNT" : stored.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception)
		{
			return View.ACCOUNT;
		}
	}

	private static class RuneLiteSettings implements Settings
	{
		private final ConfigManager configManager;
		private final NextMoveConfig config;

		private RuneLiteSettings(ConfigManager configManager, NextMoveConfig config)
		{
			this.configManager = Objects.requireNonNull(configManager);
			this.config = Objects.requireNonNull(config);
		}

		@Override
		public boolean lookupEnabled()
		{
			return config.publicLookupEnabled();
		}

		@Override
		public void setLookupEnabled(boolean enabled)
		{
			configManager.setConfiguration(CONFIG_GROUP, LOOKUP_KEY, enabled);
		}

		@Override
		public String selectedView()
		{
			return configManager.getConfiguration(CONFIG_GROUP, VIEW_KEY);
		}

		@Override
		public void setSelectedView(String selectedView)
		{
			configManager.setConfiguration(CONFIG_GROUP, VIEW_KEY, selectedView);
		}
	}

	private static class NoOpActions implements Actions
	{
		@Override
		public void load(String username, boolean friend)
		{
		}

		@Override
		public void setCurrentCharacter(String username)
		{
		}

		@Override
		public void refresh()
		{
		}

		@Override
		public void returnToCurrentCharacter()
		{
		}

		@Override
		public void clearProfile()
		{
		}
	}
}
