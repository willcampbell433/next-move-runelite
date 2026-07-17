package com.nextmove.ui;

import com.google.gson.Gson;
import com.nextmove.NextMoveConfig;
import com.nextmove.api.ProfileResponse;
import com.nextmove.profile.ProfileState;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NextMovePanelTest
{
	@Test
	public void configRequiresTheExactThirdPartyWarning() throws Exception
	{
		ConfigItem item = NextMoveConfig.class
			.getMethod("publicLookupEnabled")
			.getAnnotation(ConfigItem.class);
		assertNotNull(item);
		assertEquals(
			"This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers",
			item.warning());
	}

	@Test
	public void startsWithConsentAndNamesTheFixedHost()
	{
		Harness harness = panel(false);
		harness.onEdt(() -> harness.panel.setCurrentCharacterName("lastwilll"));

		String text = harness.text();
		assertTrue(text.contains("Load public profile"));
		assertTrue(text.contains("osrs-helper-six.vercel.app"));
		assertFalse(text.contains("Look up friend"));
		assertEquals(0, harness.actions.loadCount);
	}

	@Test
	public void consentPersistsTheOptInAndLoadsTheCurrentCharacter()
	{
		Harness harness = panel(false);
		harness.onEdt(() -> harness.panel.setCurrentCharacterName("lastwilll"));
		harness.click("Load profile");

		assertTrue(harness.settings.enabled);
		assertEquals(1, harness.actions.currentCharacterCount);
		assertEquals("lastwilll", harness.actions.lastUsername);
	}

	@Test
	public void friendStateOffersReturnToMyCharacter()
	{
		Harness harness = panel(true);
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"italiaboi69", "lastwilll", true, response)));

		assertTrue(harness.text().contains("Return to my character"));
	}

	@Test
	public void navigationStoresOnlyTheSelectedViewEnum()
	{
		Harness harness = panel(true);
		harness.click("Coach");
		assertEquals("COACH", harness.settings.selectedView);
		assertTrue(harness.text().contains("Coach"));
	}

	@Test
	public void loadedProfileKeepsAllNavigationAboveTheContent()
	{
		Harness harness = panel(true);
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		String text = harness.text();
		assertTrue(text.contains("Account"));
		assertTrue(text.contains("Coach"));
		assertTrue(text.contains("Bosses"));
		assertTrue(text.indexOf("Account") < text.indexOf("ACCOUNT POWER"));
	}

	@Test
	public void settingsIsASeparateScreenInsteadOfStackingOverTheProfile()
	{
		Harness harness = panel(true);
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		harness.click("Settings");

		assertTrue(harness.text().contains("PRIVACY"));
		assertFalse(harness.text().contains("ACCOUNT POWER"));
	}

	@Test
	public void loadedProfileCollapsesPlayerLookupUntilRequested()
	{
		Harness harness = panel(true);
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		assertEquals(0, harness.count(JTextField.class));
		harness.click("Look up another player");
		assertEquals(1, harness.count(JTextField.class));
	}

	@Test
	public void disablingLookupClearsMemoryAndReturnsToConsent()
	{
		Harness harness = panel(true);
		harness.click("Settings");
		harness.click("Disable public lookup");

		assertFalse(harness.settings.enabled);
		assertEquals(1, harness.actions.clearCount);
		assertTrue(harness.text().contains("Load public profile"));
	}

	private static Harness panel(boolean enabled)
	{
		FakeSettings settings = new FakeSettings(enabled);
		FakeActions actions = new FakeActions();
		AtomicReference<NextMovePanel> panel = new AtomicReference<>();
		onEdt(() -> panel.set(new NextMovePanel(settings, actions)));
		return new Harness(panel.get(), settings, actions);
	}

	private static ProfileResponse fixture(String name)
	{
		try (InputStream stream = NextMovePanelTest.class.getResourceAsStream("/fixtures/" + name))
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

	private static void onEdt(Runnable action)
	{
		try
		{
			SwingUtilities.invokeAndWait(action);
		}
		catch (Exception exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static List<String> textOf(Component component)
	{
		List<String> text = new ArrayList<>();
		if (component instanceof JLabel)
		{
			text.add(((JLabel) component).getText());
		}
		if (component instanceof AbstractButton)
		{
			text.add(((AbstractButton) component).getText());
		}
		if (component instanceof JTextComponent)
		{
			text.add(((JTextComponent) component).getText());
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				text.addAll(textOf(child));
			}
		}
		return text;
	}

	private static AbstractButton button(Component component, String label)
	{
		if (component instanceof AbstractButton
			&& label.equals(((AbstractButton) component).getText()))
		{
			return (AbstractButton) component;
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				AbstractButton found = button(child, label);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	private static class Harness
	{
		private final NextMovePanel panel;
		private final FakeSettings settings;
		private final FakeActions actions;

		private Harness(NextMovePanel panel, FakeSettings settings, FakeActions actions)
		{
			this.panel = panel;
			this.settings = settings;
			this.actions = actions;
		}

		void click(String label)
		{
			onEdt(() -> {
				AbstractButton found = button(panel, label);
				assertNotNull("Missing button " + label, found);
				found.doClick();
			});
		}

		void onEdt(Runnable action)
		{
			NextMovePanelTest.onEdt(action);
		}

		String text()
		{
			AtomicReference<String> value = new AtomicReference<>();
			onEdt(() -> value.set(String.join("\n", textOf(panel))));
			return value.get();
		}

		int count(Class<? extends Component> type)
		{
			AtomicReference<Integer> value = new AtomicReference<>();
			onEdt(() -> value.set(countOf(panel, type)));
			return value.get();
		}
	}

	private static int countOf(Component component, Class<? extends Component> type)
	{
		int count = type.isInstance(component) ? 1 : 0;
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				count += countOf(child, type);
			}
		}
		return count;
	}

	private static class FakeSettings implements NextMovePanel.Settings
	{
		private boolean enabled;
		private String selectedView = "ACCOUNT";

		private FakeSettings(boolean enabled)
		{
			this.enabled = enabled;
		}

		@Override
		public boolean lookupEnabled()
		{
			return enabled;
		}

		@Override
		public void setLookupEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}

		@Override
		public String selectedView()
		{
			return selectedView;
		}

		@Override
		public void setSelectedView(String selectedView)
		{
			this.selectedView = selectedView;
		}
	}

	private static class FakeActions implements NextMovePanel.Actions
	{
		private int loadCount;
		private int currentCharacterCount;
		private int clearCount;
		private String lastUsername;

		@Override
		public void load(String username, boolean friend)
		{
			loadCount += 1;
			lastUsername = username;
		}

		@Override
		public void setCurrentCharacter(String username)
		{
			currentCharacterCount += 1;
			lastUsername = username;
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
			clearCount += 1;
		}
	}
}
