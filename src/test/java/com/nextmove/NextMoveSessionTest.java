package com.nextmove;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NextMoveSessionTest
{
	private static final QuestSnapshot SNAPSHOT = new QuestSnapshot(
		"2026-07-20T15:00:00Z",
		"lastwilll",
		List.of(new QuestSnapshot.QuestEntry(
			"INTO_THE_TOMBS", "Into the Tombs", "NOT_STARTED")),
		"0.1.2");

	@Test
	public void registersAndRemovesTheNavigationButton()
	{
		Fixture fixture = new Fixture();
		fixture.session.start();
		fixture.session.start();
		assertEquals(1, fixture.toolbar.addCount);

		fixture.session.stop();
		assertEquals(1, fixture.toolbar.removeCount);
		assertEquals(1, fixture.profile.closeCount);
	}

	@Test
	public void showsAndLoadsTheLoggedInCharacterOnce()
	{
		Fixture fixture = new Fixture();
		fixture.session.start();
		fixture.session.loggedIn("lastwilll", SNAPSHOT);
		fixture.session.loggedIn("lastwilll", SNAPSHOT);

		assertEquals("lastwilll", fixture.profile.shownUsername);
		assertEquals(1, fixture.profile.loadCount);
		assertEquals("lastwilll", fixture.profile.loadedUsername);
		assertEquals(SNAPSHOT, fixture.profile.loadedSnapshot);
	}

	@Test
	public void ignoresInvalidCharacterNames()
	{
		Fixture fixture = new Fixture();
		fixture.session.start();
		fixture.session.loggedIn("name-that-is-too-long", SNAPSHOT);

		assertEquals(0, fixture.profile.loadCount);
	}

	@Test
	public void characterSwitchLoadsTheNewNameAndLogoutClearsIt()
	{
		Fixture fixture = new Fixture();
		fixture.session.start();
		fixture.session.loggedIn("first", SNAPSHOT);
		fixture.session.loggedIn("second", SNAPSHOT);
		fixture.session.loggedOut();

		assertEquals(2, fixture.profile.loadCount);
		assertEquals("second", fixture.profile.loadedUsername);
		assertEquals(1, fixture.profile.clearCharacterCount);
	}

	private static class Fixture
	{
		private final FakeToolbar toolbar = new FakeToolbar();
		private final FakeProfile profile = new FakeProfile();
		private final NextMoveSession session;

		private Fixture()
		{
			session = new NextMoveSession(toolbar, profile);
		}
	}

	private static class FakeToolbar implements NextMoveSession.ToolbarPort
	{
		private int addCount;
		private int removeCount;

		@Override
		public void add()
		{
			addCount += 1;
		}

		@Override
		public void remove()
		{
			removeCount += 1;
		}
	}

	private static class FakeProfile implements NextMoveSession.ProfilePort
	{
		private String shownUsername;
		private String loadedUsername;
		private QuestSnapshot loadedSnapshot;
		private int loadCount;
		private int clearCharacterCount;
		private int closeCount;

		@Override
		public void showCurrentCharacter(String username)
		{
			shownUsername = username;
		}

		@Override
		public void loadCurrentCharacter(String username, QuestSnapshot snapshot)
		{
			loadedUsername = username;
			loadedSnapshot = snapshot;
			loadCount += 1;
		}

		@Override
		public void clearCurrentCharacter()
		{
			shownUsername = null;
			clearCharacterCount += 1;
		}

		@Override
		public void close()
		{
			closeCount += 1;
		}
	}
}
