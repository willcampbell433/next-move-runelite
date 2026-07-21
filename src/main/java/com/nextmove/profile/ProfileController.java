package com.nextmove.profile;

import com.nextmove.QuestSnapshot;
import com.nextmove.api.NextMoveClient;
import com.nextmove.api.ProfileFailure;
import com.nextmove.api.ProfileRequest;
import com.nextmove.api.ProfileResponse;
import com.nextmove.completion.CompletedRecommendation;
import com.nextmove.completion.CompletionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.SwingUtilities;

public class ProfileController
{
	interface Loader
	{
		ProfileRequest load(
			String username,
			QuestSnapshot snapshot,
			NextMoveClient.Callback callback);
	}

	private final Loader loader;
	private final ProfileView view;
	private final CompletionRepository completions;
	private long generation;
	private ProfileRequest inFlight;
	private ProfileState state = ProfileState.notLoaded(null);
	private ProfileResponse lastSuccessfulProfile;
	private String lastSuccessfulUsername;
	private String currentCharacter;
	private QuestSnapshot currentQuestSnapshot;
	private boolean closed;

	public ProfileController(NextMoveClient client, ProfileView view)
	{
		this(client, view, CompletionRepository.none());
	}

	public ProfileController(
		NextMoveClient client,
		ProfileView view,
		CompletionRepository completions)
	{
		this(
			(username, snapshot, callback) -> client.load(username, snapshot, callback),
			view,
			completions);
	}

	ProfileController(Loader loader, ProfileView view)
	{
		this(loader, view, CompletionRepository.none());
	}

	ProfileController(
		Loader loader,
		ProfileView view,
		CompletionRepository completions)
	{
		this.loader = Objects.requireNonNull(loader);
		this.view = Objects.requireNonNull(view);
		this.completions = Objects.requireNonNull(completions);
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
		QuestSnapshot baseSnapshot = effectiveFriend
			? null
			: currentCharacter != null && sameAccount(selected, currentCharacter)
				? currentQuestSnapshot
				: null;
		List<String> completedIds = baseSnapshot == null
			? List.of()
			: completions.completedIds(selected);
		QuestSnapshot requestSnapshot = baseSnapshot == null
			? null
			: completedIds.isEmpty()
				? baseSnapshot
				: baseSnapshot.withCompletedRecommendationIds(completedIds);
		inFlight = loader.load(selected, requestSnapshot, new NextMoveClient.Callback()
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
		setCurrentCharacter(username, null);
	}

	public synchronized void setCurrentCharacter(
		String username,
		QuestSnapshot snapshot)
	{
		String selected = username == null ? "" : username.trim();
		if (selected.isEmpty())
		{
			clearCurrentCharacter();
			return;
		}
		currentCharacter = selected;
		currentQuestSnapshot = snapshot;
		if (state.isFriendActive())
		{
			render(state.withCurrentCharacter(selected));
			return;
		}
		load(selected, false);
	}

	public synchronized void refreshCurrentCharacter(QuestSnapshot snapshot)
	{
		if (currentCharacter == null)
		{
			return;
		}
		currentQuestSnapshot = Objects.requireNonNull(snapshot);
		load(currentCharacter, false);
	}

	public synchronized boolean isFriendActive()
	{
		return state.isFriendActive();
	}

	public synchronized boolean isCurrentCharacterActive()
	{
		return canEditCurrentCharacter();
	}

	public synchronized List<CompletedRecommendation> completedRecommendations(
		String username)
	{
		return currentCharacter != null && sameAccount(username, currentCharacter)
			? completions.load(currentCharacter)
			: List.of();
	}

	public synchronized void markDone(ProfileResponse.Recommendation recommendation)
	{
		if (!canEditCurrentCharacter() || recommendation == null)
		{
			return;
		}
		completions.markDone(currentCharacter, recommendation, Instant.now());
		refresh();
	}

	public synchronized void restoreCompleted(String recommendationId)
	{
		if (!canEditCurrentCharacter() || recommendationId == null)
		{
			return;
		}
		completions.restore(currentCharacter, recommendationId);
		refresh();
	}

	public synchronized void clearCurrentCharacter()
	{
		currentCharacter = null;
		currentQuestSnapshot = null;
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

	public synchronized void close()
	{
		closed = true;
		generation += 1;
		cancelInFlight();
		lastSuccessfulProfile = null;
		lastSuccessfulUsername = null;
		currentQuestSnapshot = null;
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

	private boolean canEditCurrentCharacter()
	{
		return !state.isFriendActive()
			&& currentCharacter != null
			&& sameAccount(state.getActiveUsername(), currentCharacter);
	}

	private static boolean sameAccount(String left, String right)
	{
		return normalizeAccount(left).equals(normalizeAccount(right));
	}

	private static String normalizeAccount(String value)
	{
		return value == null
			? ""
			: value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
	}
}
