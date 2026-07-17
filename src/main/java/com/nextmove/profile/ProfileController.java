package com.nextmove.profile;

import com.nextmove.api.NextMoveClient;
import com.nextmove.api.ProfileFailure;
import com.nextmove.api.ProfileRequest;
import com.nextmove.api.ProfileResponse;
import java.util.Objects;
import javax.swing.SwingUtilities;

public class ProfileController
{
	interface Loader
	{
		ProfileRequest load(String username, NextMoveClient.Callback callback);
	}

	private final Loader loader;
	private final ProfileView view;
	private long generation;
	private ProfileRequest inFlight;
	private ProfileState state = ProfileState.notLoaded(null);
	private ProfileResponse lastSuccessfulProfile;
	private String lastSuccessfulUsername;
	private String currentCharacter;
	private boolean closed;

	public ProfileController(NextMoveClient client, ProfileView view)
	{
		this(client::load, view);
	}

	ProfileController(Loader loader, ProfileView view)
	{
		this.loader = Objects.requireNonNull(loader);
		this.view = Objects.requireNonNull(view);
	}

	public synchronized void load(String username, boolean friend)
	{
		if (closed)
		{
			return;
		}
		String selected = username == null ? "" : username.trim();
		boolean effectiveFriend = friend
			&& currentCharacter != null
			&& !selected.equalsIgnoreCase(currentCharacter);
		if (state.getStatus() == ProfileState.Status.LOADING
			&& Objects.equals(state.getActiveUsername(), selected)
			&& state.isFriendActive() == effectiveFriend)
		{
			return;
		}

		long requestGeneration = ++generation;
		cancelInFlight();
		render(ProfileState.loading(selected, currentCharacter, effectiveFriend));
		inFlight = loader.load(selected, new NextMoveClient.Callback()
		{
			@Override
			public void onSuccess(ProfileResponse response)
			{
				SwingUtilities.invokeLater(() -> acceptSuccess(
					requestGeneration,
					selected,
					effectiveFriend,
					response));
			}

			@Override
			public void onFailure(ProfileFailure failure)
			{
				SwingUtilities.invokeLater(() -> acceptFailure(
					requestGeneration,
					selected,
					effectiveFriend,
					failure));
			}
		});
	}

	public synchronized void refresh()
	{
		if (state.getActiveUsername() != null)
		{
			load(state.getActiveUsername(), state.isFriendActive());
		}
	}

	public synchronized void setCurrentCharacter(String username)
	{
		String selected = username == null ? "" : username.trim();
		if (selected.isEmpty())
		{
			clearCurrentCharacter();
			return;
		}
		currentCharacter = selected;
		if (state.isFriendActive())
		{
			render(state.withCurrentCharacter(selected));
			return;
		}
		load(selected, false);
	}

	public synchronized void clearCurrentCharacter()
	{
		currentCharacter = null;
		if (state.isFriendActive())
		{
			render(state.withCurrentCharacter(null));
			return;
		}
		resetInMemory();
	}

	public synchronized void returnToCurrentCharacter()
	{
		if (currentCharacter == null)
		{
			resetInMemory();
			return;
		}
		load(currentCharacter, false);
	}

	public synchronized void clearProfile()
	{
		resetInMemory();
	}

	public synchronized void close()
	{
		closed = true;
		generation += 1;
		cancelInFlight();
		lastSuccessfulProfile = null;
		lastSuccessfulUsername = null;
		state = ProfileState.notLoaded(currentCharacter);
	}

	private synchronized void acceptSuccess(
		long requestGeneration,
		String username,
		boolean friend,
		ProfileResponse response)
	{
		if (closed || requestGeneration != generation)
		{
			return;
		}
		inFlight = null;
		lastSuccessfulProfile = response;
		lastSuccessfulUsername = username;
		render(ProfileState.loaded(username, currentCharacter, friend, response));
	}

	private synchronized void acceptFailure(
		long requestGeneration,
		String username,
		boolean friend,
		ProfileFailure failure)
	{
		if (closed || requestGeneration != generation)
		{
			return;
		}
		inFlight = null;
		if (lastSuccessfulProfile != null && Objects.equals(lastSuccessfulUsername, username))
		{
			render(ProfileState.stale(
				username,
				currentCharacter,
				friend,
				lastSuccessfulProfile,
				failure.getMessage(),
				failure.getRetryAfterSeconds()));
			return;
		}

		render(ProfileState.failure(
			statusFor(failure.getKind()),
			username,
			currentCharacter,
			friend,
			failure.getMessage(),
			failure.getRetryAfterSeconds()));
	}

	private static ProfileState.Status statusFor(ProfileFailure.Kind kind)
	{
		switch (kind)
		{
			case NOT_FOUND:
				return ProfileState.Status.NOT_FOUND;
			case RATE_LIMITED:
				return ProfileState.Status.RATE_LIMITED;
			case INCOMPATIBLE:
			case MALFORMED:
				return ProfileState.Status.INCOMPATIBLE_RESPONSE;
			case INVALID_USERNAME:
			case UNAVAILABLE:
			default:
				return ProfileState.Status.UNAVAILABLE;
		}
	}

	private void resetInMemory()
	{
		generation += 1;
		cancelInFlight();
		lastSuccessfulProfile = null;
		lastSuccessfulUsername = null;
		render(ProfileState.notLoaded(currentCharacter));
	}

	private void cancelInFlight()
	{
		if (inFlight != null)
		{
			inFlight.cancel();
			inFlight = null;
		}
	}

	private void render(ProfileState next)
	{
		state = next;
		if (SwingUtilities.isEventDispatchThread())
		{
			view.render(next);
		}
		else
		{
			SwingUtilities.invokeLater(() -> view.render(next));
		}
	}
}
