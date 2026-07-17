package com.nextmove;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NextMoveSessionTest
{
	@Test
	public void registersAndRemovesTheNavigationButton()
	{
		Fixture fixture = new Fixture(false);
		fixture.session.start();
		fixture.session.start();
		assertEquals(1, fixture.toolbar.addCount);

		fixture.session.stop();
		assertEquals(1, fixture.toolbar.removeCount);
		assertEquals(1, fixture.profile.closeCount);
	}

	@Test
	public void showsButDoesNotLoadACharacterBeforeOptIn()
	{
		Fixture fixture = new Fixture(false);
		fixture.session.start();
		fixture.session.loggedIn("lastwilll");

		assertEquals("lastwilll", fixture.profile.shownUsername);
		assertEquals(0, fixture.profile.loadCount);
	}

	@Test
	public void loadsOnceAfterOptInAndLogin()
	{
		Fixture fixture = new Fixture(true);
		fixture.session.start();
		fixture.session.loggedIn("lastwilll");
		fixture.session.loggedIn("lastwilll");

		assertEquals(1, fixture.profile.loadCount);
		assertEquals("lastwilll", fixture.profile.loadedUsername);
	}

	@Test
	public void consentAfterLoginLoadsTheVisibleCharacter()
	{
		Fixture fixture = new Fixture(false);
		fixture.session.start();
		fixture.session.loggedIn("lastwilll");
		fixture.enabled.set(true);
		fixture.session.consentChanged(true);

		assertEquals(1, fixture.profile.loadCount);
		assertEquals("lastwilll", fixture.profile.loadedUsername);
	}

	@Test
	public void characterSwitchLoadsTheNewNameAndLogoutClearsIt()
	{
		Fixture fixture = new Fixture(true);
		fixture.session.start();
		fixture.session.loggedIn("first");
		fixture.session.loggedIn("second");
		fixture.session.loggedOut();

		assertEquals(2, fixture.profile.loadCount);
		assertEquals("second", fixture.profile.loadedUsername);
		assertEquals(1, fixture.profile.clearCharacterCount);
	}

	@Test
	public void disablingConsentClearsOnlyTheInMemoryProfile()
	{
		Fixture fixture = new Fixture(true);
		fixture.session.start();
		fixture.session.loggedIn("lastwilll");
		fixture.enabled.set(false);
		fixture.session.consentChanged(false);

		assertEquals(1, fixture.profile.clearProfileCount);
		assertEquals("lastwilll", fixture.profile.shownUsername);
	}

	private static class Fixture
	{
		private final AtomicBoolean enabled;
		private final FakeToolbar toolbar = new FakeToolbar();
		private final FakeProfile profile = new FakeProfile();
		private final NextMoveSession session;

		private Fixture(boolean enabled)
		{
			this.enabled = new AtomicBoolean(enabled);
			session = new NextMoveSession(toolbar, profile, this.enabled::get);
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
		private int loadCount;
		private int clearCharacterCount;
		private int clearProfileCount;
		private int closeCount;

		@Override
		public void showCurrentCharacter(String username)
		{
			shownUsername = username;
		}

		@Override
		public void loadCurrentCharacter(String username)
		{
			loadedUsername = username;
			loadCount += 1;
		}

		@Override
		public void clearCurrentCharacter()
		{
			shownUsername = null;
			clearCharacterCount += 1;
		}

		@Override
		public void clearProfile()
		{
			clearProfileCount += 1;
		}

		@Override
		public void close()
		{
			closeCount += 1;
		}
	}
}
