package com.nextmove;

import java.util.Objects;
import java.util.regex.Pattern;

final class NextMoveSession
{
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9 _-]{1,12}");

	interface ToolbarPort
	{
		void add();

		void remove();
	}

	interface ProfilePort
	{
		void showCurrentCharacter(String username);

		void loadCurrentCharacter(String username, QuestSnapshot snapshot);

		void clearCurrentCharacter();

		void close();
	}

	private final ToolbarPort toolbar;
	private final ProfilePort profile;
	private boolean started;
	private String currentUsername;
	private String lastLoadedUsername;

	NextMoveSession(
		ToolbarPort toolbar,
		ProfilePort profile)
	{
		this.toolbar = Objects.requireNonNull(toolbar);
		this.profile = Objects.requireNonNull(profile);
	}

	void start()
	{
		if (started)
		{
			return;
		}
		started = true;
		toolbar.add();
	}

	void loggedIn(String username, QuestSnapshot snapshot)
	{
		if (!started)
		{
			return;
		}
		String selected = username == null ? "" : username.trim();
		if (!USERNAME.matcher(selected).matches())
		{
			return;
		}
		boolean changed = !selected.equals(currentUsername);
		currentUsername = selected;
		if (changed)
		{
			profile.showCurrentCharacter(selected);
			lastLoadedUsername = null;
		}
		if (!selected.equals(lastLoadedUsername))
		{
			profile.loadCurrentCharacter(selected, Objects.requireNonNull(snapshot));
			lastLoadedUsername = selected;
		}
	}

	void loggedOut()
	{
		if (!started || currentUsername == null)
		{
			return;
		}
		currentUsername = null;
		lastLoadedUsername = null;
		profile.clearCurrentCharacter();
	}

	void stop()
	{
		if (!started)
		{
			return;
		}
		started = false;
		currentUsername = null;
		lastLoadedUsername = null;
		profile.close();
		toolbar.remove();
	}
}
