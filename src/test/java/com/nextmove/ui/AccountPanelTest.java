package com.nextmove.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nextmove.api.ProfileResponse;
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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountPanelTest
{
	@Test
	public void rendersTheFullAccountVerdictAndTrophies()
	{
		String text = render(fixture("full-profile.json").getProfile());
		assertTrue(text.contains("92.6 / 100"));
		assertTrue(text.contains("Giga Chad"));
		assertTrue(text.contains("Skills"));
		assertTrue(text.contains("Combat"));
		assertTrue(text.contains("Boss Bravery"));
		assertTrue(text.contains("Raid Reputation"));
		assertTrue(text.contains("Quests"));
		assertTrue(text.contains("Start the Inferno cape grind"));
		assertTrue(text.contains("Fire Cape Acquired"));
		assertTrue(text.contains("How is this score calculated?"));
		assertFalse(text.contains("Open on Next Move"));
		assertFalse(text.contains("•"));
		assertFalse(text.contains("width: 205px"));
	}

	@Test
	public void labelsUnavailableQuestDataWithoutScoringItZero()
	{
		String text = render(fixture("partial-profile.json").getProfile());
		assertTrue(text.contains("Partial public score"));
		assertTrue(text.contains("Quests — Unavailable"));
		assertFalse(text.contains("Quests 0"));
	}

	@Test
	public void keepsTheSidebarCompactWhenManyTrophiesExist()
	{
		String text = render(fixtureWithTrophies(6).getProfile());

		assertTrue(text.contains("Trophy 4"));
		assertFalse(text.contains("Trophy 5"));
		assertTrue(text.contains("+ 2 more on the website"));
	}

	@Test
	public void alignsTheAccountCardLeftAndNormalizesUnsupportedQuotes()
	{
		AccountPanel panel = renderPanel(fixtureWithVerdict(
			"Calls every eighty-hour grind “a quick unlock.”").getProfile());
		String text = text(panel);

		assertEquals(Component.LEFT_ALIGNMENT, panel.getAlignmentX(), 0.0f);
		assertFalse(text.contains("“"));
		assertFalse(text.contains("”"));
		assertTrue(text.contains("&quot;a quick unlock.&quot;"));
	}

	@Test
	public void scoreGuideActionIsFullWidthCenteredAndSeparatedFromFollowingContent()
	{
		AccountPanel panel = renderPanel(fixture("full-profile.json").getProfile());
		JButton button = findButton(panel, "How is this score calculated?");

		assertEquals(Integer.MAX_VALUE, button.getMaximumSize().width);
		assertEquals(SwingConstants.CENTER, button.getHorizontalAlignment());
		Component trailingSpace = panel.getComponent(panel.getComponentCount() - 1);
		assertTrue(trailingSpace instanceof Box.Filler);
		assertTrue(trailingSpace.getPreferredSize().height >= 6);
	}

	private static String render(ProfileResponse.Profile profile)
	{
		return text(renderPanel(profile));
	}

	private static AccountPanel renderPanel(ProfileResponse.Profile profile)
	{
		AtomicReference<AccountPanel> rendered = new AtomicReference<>();
		onEdt(() -> {
			AccountPanel panel = new AccountPanel();
			panel.render(profile);
			rendered.set(panel);
		});
		return rendered.get();
	}

	private static String text(Component panel)
	{
		AtomicReference<String> rendered = new AtomicReference<>();
		onEdt(() -> rendered.set(String.join("\n", textOf(panel))));
		return rendered.get();
	}

	private static ProfileResponse fixture(String name)
	{
		try (InputStream stream = AccountPanelTest.class.getResourceAsStream("/fixtures/" + name))
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

	private static ProfileResponse fixtureWithTrophies(int count)
	{
		try (InputStream stream = AccountPanelTest.class.getResourceAsStream("/fixtures/full-profile.json"))
		{
			JsonObject root = new JsonParser().parse(
				new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
			JsonArray trophies = new JsonArray();
			for (int index = 1; index <= count; index += 1)
			{
				JsonObject trophy = new JsonObject();
				trophy.addProperty("id", "trophy-" + index);
				trophy.addProperty("label", "Trophy " + index);
				trophy.addProperty("iconKey", "TEST");
				trophies.add(trophy);
			}
			root.getAsJsonObject("profile")
				.getAsJsonObject("bosses")
				.add("trophies", trophies);
			return new Gson().fromJson(root, ProfileResponse.class);
		}
		catch (IOException exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static ProfileResponse fixtureWithVerdict(String verdict)
	{
		try (InputStream stream = AccountPanelTest.class.getResourceAsStream("/fixtures/full-profile.json"))
		{
			JsonObject root = new JsonParser().parse(
				new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
			root.getAsJsonObject("profile")
				.getAsJsonObject("account")
				.addProperty("verdict", verdict);
			return new Gson().fromJson(root, ProfileResponse.class);
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
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				text.addAll(textOf(child));
			}
		}
		return text;
	}

	private static JButton findButton(Container container, String text)
	{
		for (Component child : container.getComponents())
		{
			if (child instanceof JButton && text.equals(((JButton) child).getText()))
			{
				return (JButton) child;
			}
			if (child instanceof Container)
			{
				try
				{
					return findButton((Container) child, text);
				}
				catch (IllegalStateException ignored)
				{
					// Continue searching sibling containers.
				}
			}
		}
		throw new IllegalStateException("Missing button " + text);
	}
}
