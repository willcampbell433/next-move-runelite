package com.nextmove.ui;

import com.google.gson.Gson;
import com.nextmove.api.ProfileResponse;
import com.nextmove.profile.ProfileState;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class NextMovePanelTest
{
	@Test
	public void startsWithTheMeaningfulProfileInterface()
	{
		Harness harness = panel();
		harness.onEdt(() -> harness.panel.setCurrentCharacterName("lastwilll"));

		String text = harness.text();
		assertTrue(text.contains("Power"));
		assertTrue(text.contains("Coach"));
		assertTrue(text.contains("Bosses"));
		assertTrue(text.contains("Look up friend"));
		assertFalse(text.contains("Load public profile"));
	}

	@Test
	public void friendStateOffersReturnToMyCharacter()
	{
		Harness harness = panel();
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"italiaboi69", "lastwilll", true, response)));

		assertTrue(harness.text().contains("Return to my character"));
	}

	@Test
	public void navigationStoresOnlyTheSelectedViewEnum()
	{
		Harness harness = panel();
		harness.click("Coach");
		assertEquals("COACH", harness.settings.selectedView);
		assertTrue(harness.text().contains("Coach"));
	}

	@Test
	public void loadedProfileKeepsAllNavigationAboveTheContent()
	{
		Harness harness = panel();
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		String text = harness.text();
		assertTrue(text.contains("Power"));
		assertTrue(text.contains("Coach"));
		assertTrue(text.contains("Bosses"));
		assertTrue(text.indexOf("Power") < text.indexOf("ACCOUNT POWER"));
		assertEquals(Component.LEFT_ALIGNMENT, harness.alignmentOf("lastwilll · My character"), 0.0f);
		for (String label : new String[] {"Power", "Coach", "Bosses"})
		{
			Insets margin = harness.buttonMargin(label);
			assertTrue(margin.left <= 4);
			assertTrue(margin.right <= 4);
		}
	}

	@Test
	public void coachShowsTheFullRecommendationFeed()
	{
		Harness harness = panel();
		ProfileResponse jared = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"italiaboi69", "lastwilll", true, jared)));
		harness.click("Coach");
		assertTrue(harness.text().contains("Start the Inferno cape grind"));
		assertTrue(harness.text().contains("Push Ranged to 100"));
		assertTrue(harness.text().contains("SHOWING 2 OF 2 IDEAS"));
		assertFalse(harness.text().contains("Next idea"));
		assertFalse(harness.text().contains("OTHER IDEAS"));
	}

	@Test
	public void settingsIsASeparateScreenInsteadOfStackingOverTheProfile()
	{
		Harness harness = panel();
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
		Harness harness = panel();
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		assertEquals(0, harness.count(JTextField.class));
		harness.click("Look up another player");
		assertEquals(1, harness.count(JTextField.class));
		JLabel lookupLabel = harness.label("Look up friend");
		assertEquals(JLabel.CENTER, lookupLabel.getHorizontalAlignment());
		assertEquals(Component.LEFT_ALIGNMENT, lookupLabel.getAlignmentX(), 0.0f);
	}

	@Test
	public void playerLookupUsesASpacedFullWidthActionRow()
	{
		Harness harness = panel();
		ProfileResponse response = fixture("full-profile.json");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));
		harness.click("Look up another player");

		JTextField username = harness.textField();
		AbstractButton lookup = harness.button("Look up player");
		assertFalse("The action needs its own full-width row", username.getParent() == lookup.getParent());
		assertTrue(lookup.getParent().getLayout() instanceof BoxLayout);
		assertEquals(Integer.MAX_VALUE, lookup.getMaximumSize().width);

		Component[] lookupChildren = username.getParent().getComponents();
		int fieldIndex = indexOf(lookupChildren, username);
		int rowIndex = indexOf(lookupChildren, lookup.getParent());
		assertEquals(fieldIndex + 2, rowIndex);
		assertTrue(lookupChildren[fieldIndex + 1] instanceof Box.Filler);
		assertTrue(lookupChildren[fieldIndex + 1].getPreferredSize().height >= 6);
	}

	@Test
	public void settingsExplainsTheFixedServiceWithoutADisableToggle()
	{
		Harness harness = panel();
		harness.click("Settings");

		String text = harness.text();
		assertTrue(text.contains("osrsnextmove.com"));
		assertTrue(text.contains("official public Hiscores"));
		assertTrue(text.contains("logged-in character"));
		assertTrue(text.contains("quest progress"));
		assertTrue(text.contains("Friend lookups use Hiscores only"));
		assertFalse(text.contains("Disable public lookup"));
	}

	@Test
	public void questAwareCurrentCharacterExplainsItsCombinedSources()
	{
		Harness harness = panel();
		ProfileResponse response = fixtureForQuestAware("full-profile.json", "lastwilll");
		harness.onEdt(() -> harness.panel.render(ProfileState.loaded(
			"lastwilll", "lastwilll", false, response)));

		assertTrue(harness.text().contains("RuneLite quests · Hiscores skills"));
	}

	private static Harness panel()
	{
		FakeSettings settings = new FakeSettings();
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

	private static ProfileResponse fixtureForUsername(String name, String username)
	{
		try (InputStream stream = NextMovePanelTest.class.getResourceAsStream("/fixtures/" + name))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing fixture " + name);
			}
			String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
				.replace("\"username\": \"italiaboi69\"", "\"username\": \"" + username + "\"");
			return new Gson().fromJson(json, ProfileResponse.class);
		}
		catch (IOException exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static ProfileResponse fixtureForQuestAware(String name, String username)
	{
		try (InputStream stream = NextMovePanelTest.class.getResourceAsStream("/fixtures/" + name))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing fixture " + name);
			}
			String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
				.replace("\"username\": \"italiaboi69\"", "\"username\": \"" + username + "\"")
				.replace("\"dataSource\": \"HISCORES\"", "\"dataSource\": \"RUNELITE\"")
				.replace("\"questData\": \"MISSING\"", "\"questData\": \"AVAILABLE\"");
			return new Gson().fromJson(json, ProfileResponse.class);
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
				AbstractButton found = NextMovePanelTest.button(panel, label);
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

		float alignmentOf(String label)
		{
			AtomicReference<Float> value = new AtomicReference<>();
			onEdt(() -> {
				Component found = componentWithText(panel, label);
				assertNotNull("Missing component " + label, found);
				value.set(found.getAlignmentX());
			});
			return value.get();
		}

		Insets buttonMargin(String label)
		{
			AtomicReference<Insets> value = new AtomicReference<>();
			onEdt(() -> {
				AbstractButton found = NextMovePanelTest.button(panel, label);
				assertNotNull("Missing button " + label, found);
				value.set(found.getMargin());
			});
			return value.get();
		}

		AbstractButton button(String label)
		{
			AtomicReference<AbstractButton> value = new AtomicReference<>();
			onEdt(() -> {
				AbstractButton found = NextMovePanelTest.button(panel, label);
				assertNotNull("Missing button " + label, found);
				value.set(found);
			});
			return value.get();
		}

		JTextField textField()
		{
			AtomicReference<JTextField> value = new AtomicReference<>();
			onEdt(() -> value.set((JTextField) componentOfType(panel, JTextField.class)));
			assertNotNull("Missing player lookup field", value.get());
			return value.get();
		}

		JLabel label(String text)
		{
			AtomicReference<JLabel> value = new AtomicReference<>();
			onEdt(() -> {
				Component found = componentWithText(panel, text);
				assertTrue("Expected label " + text, found instanceof JLabel);
				value.set((JLabel) found);
			});
			return value.get();
		}
	}

	private static Component componentWithText(Component component, String text)
	{
		if (component instanceof JLabel && text.equals(((JLabel) component).getText()))
		{
			return component;
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				Component found = componentWithText(child, text);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
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

	private static Component componentOfType(Component component, Class<? extends Component> type)
	{
		if (type.isInstance(component))
		{
			return component;
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				Component found = componentOfType(child, type);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	private static int indexOf(Component[] components, Component target)
	{
		for (int index = 0; index < components.length; index += 1)
		{
			if (components[index] == target)
			{
				return index;
			}
		}
		return -1;
	}

	private static class FakeSettings implements NextMovePanel.Settings
	{
		private String selectedView = "ACCOUNT";

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
		public void refresh()
		{
		}

		@Override
		public void returnToCurrentCharacter()
		{
		}

	}
}
