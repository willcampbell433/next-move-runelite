package com.nextmove;

import java.util.Objects;
import java.util.function.BooleanSupplier;
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

		void loadCurrentCharacter(String username);

		void clearCurrentCharacter();

		void clearProfile();

		void close();
	}

	private final ToolbarPort toolbar;
	private final ProfilePort profile;
	private final BooleanSupplier lookupEnabled;
	private boolean started;
	private String currentUsername;
	private String lastLoadedUsername;

	NextMoveSession(
		ToolbarPort toolbar,
		ProfilePort profile,
		BooleanSupplier lookupEnabled)
	{
		this.toolbar = Objects.requireNonNull(toolbar);
		this.profile = Objects.requireNonNull(profile);
		this.lookupEnabled = Objects.requireNonNull(lookupEnabled);
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

	void loggedIn(String username)
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
		if (lookupEnabled.getAsBoolean() && !selected.equals(lastLoadedUsername))
		{
			profile.loadCurrentCharacter(selected);
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

	void consentChanged(boolean enabled)
	{
		if (!started)
		{
			return;
		}
		if (!enabled)
		{
			lastLoadedUsername = null;
			profile.clearProfile();
			return;
		}
		if (currentUsername != null && !currentUsername.equals(lastLoadedUsername))
		{
			profile.loadCurrentCharacter(currentUsername);
			lastLoadedUsername = currentUsername;
		}
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
