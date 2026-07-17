package com.nextmove.profile;

import com.google.gson.Gson;
import com.nextmove.api.NextMoveClient;
import com.nextmove.api.ProfileFailure;
import com.nextmove.api.ProfileRequest;
import com.nextmove.api.ProfileResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ProfileControllerTest
{
	@Test
	public void doesNotRequestBeforeExplicitLoad()
	{
		FakeLoader loader = new FakeLoader();
		new ProfileController(loader, new RecordingView());
		assertEquals(0, loader.calls.size());
	}

	@Test
	public void rendersNormalSuccess()
	{
		FakeLoader loader = new FakeLoader();
		RecordingView view = new RecordingView();
		ProfileController controller = new ProfileController(loader, view);

		controller.load("italiaboi69", false);
		flushEdt();
		assertEquals(ProfileState.Status.LOADING, view.latest.getStatus());
		loader.succeed(0, fixture("full-profile.json"));
		flushEdt();

		assertEquals(ProfileState.Status.LOADED, view.latest.getStatus());
		assertEquals("italiaboi69", view.latest.getActiveUsername());
		assertFalse(view.latest.isFriendActive());
	}

	@Test
	public void refreshFailureRetainsOnlyTheSameProfileAsStale()
	{
		FakeLoader loader = new FakeLoader();
		RecordingView view = new RecordingView();
		ProfileController controller = new ProfileController(loader, view);
		ProfileResponse profile = fixture("full-profile.json");

		controller.load("italiaboi69", false);
		loader.succeed(0, profile);
		flushEdt();
		controller.refresh();
		loader.fail(1, ProfileFailure.Kind.UNAVAILABLE);
		flushEdt();

		assertEquals(ProfileState.Status.LOADED_STALE, view.latest.getStatus());
		assertSame(profile, view.latest.getProfile());
	}

	@Test
	public void friendLookupCanReturnToTheCurrentCharacter()
	{
		FakeLoader loader = new FakeLoader();
		RecordingView view = new RecordingView();
		ProfileController controller = new ProfileController(loader, view);

		controller.setCurrentCharacter("lastwilll");
		controller.load("no_noobs10", true);
		flushEdt();
		assertTrue(view.latest.isFriendActive());
		assertEquals("no_noobs10", view.latest.getActiveUsername());

		controller.returnToCurrentCharacter();
		flushEdt();
		assertFalse(view.latest.isFriendActive());
		assertEquals("lastwilll", view.latest.getActiveUsername());
	}

	@Test
	public void ignoresLateResponsesAfterAnotherCharacterLoads()
	{
		FakeLoader loader = new FakeLoader();
		RecordingView view = new RecordingView();
		ProfileController controller = new ProfileController(loader, view);
		ProfileResponse firstProfile = fixture("full-profile.json");

		controller.load("first_player", false);
		controller.load("second", false);
		loader.succeed(0, firstProfile);
		flushEdt();

		assertEquals("second", view.latest.getActiveUsername());
		assertNotSame(firstProfile, view.latest.getProfile());
		loader.succeed(1, fixture("partial-profile.json"));
		flushEdt();
		assertEquals(ProfileState.Status.LOADED, view.latest.getStatus());
		assertEquals("second", view.latest.getActiveUsername());
	}

	@Test
	public void coalescesDuplicateInflightLoads()
	{
		FakeLoader loader = new FakeLoader();
		ProfileController controller = new ProfileController(loader, new RecordingView());
		controller.load("lastwilll", false);
		controller.load("lastwilll", false);
		assertEquals(1, loader.calls.size());
	}

	@Test
	public void closeCancelsAndInvalidatesTheRequest()
	{
		FakeLoader loader = new FakeLoader();
		RecordingView view = new RecordingView();
		ProfileController controller = new ProfileController(loader, view);
		controller.load("lastwilll", false);
		controller.close();

		assertTrue(loader.calls.get(0).request.canceled);
		loader.succeed(0, fixture("full-profile.json"));
		flushEdt();
		assertNull(view.latest.getProfile());
	}

	private static ProfileResponse fixture(String name)
	{
		try (InputStream stream = ProfileControllerTest.class.getResourceAsStream("/fixtures/" + name))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing fixture " + name);
			}
			return new Gson().fromJson(
				new InputStreamReader(stream, StandardCharsets.UTF_8),
				ProfileResponse.class);
		}
		catch (IOException exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static void flushEdt()
	{
		try
		{
			SwingUtilities.invokeAndWait(() -> { });
		}
		catch (Exception exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static class RecordingView implements ProfileView
	{
		private ProfileState latest = ProfileState.notLoaded(null);

		@Override
		public void render(ProfileState state)
		{
			latest = state;
		}
	}

	private static class FakeLoader implements ProfileController.Loader
	{
		private final List<PendingCall> calls = new ArrayList<>();

		@Override
		public ProfileRequest load(String username, NextMoveClient.Callback callback)
		{
			PendingCall call = new PendingCall(username, callback);
			calls.add(call);
			return call.request;
		}

		void succeed(int index, ProfileResponse response)
		{
			calls.get(index).callback.onSuccess(response);
		}

		void fail(int index, ProfileFailure.Kind kind)
		{
			calls.get(index).callback.onFailure(new ProfileFailure(kind, "test failure", null));
		}
	}

	private static class PendingCall
	{
		private final String username;
		private final NextMoveClient.Callback callback;
		private final FakeRequest request = new FakeRequest();

		private PendingCall(String username, NextMoveClient.Callback callback)
		{
			this.username = username;
			this.callback = callback;
		}
	}

	private static class FakeRequest implements ProfileRequest
	{
		private boolean canceled;

		@Override
		public void cancel()
		{
			canceled = true;
		}
	}
}
