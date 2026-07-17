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
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccountPanelTest
{
	@Test
	public void rendersTheFullAccountVerdictAndTrophies()
	{
		String text = render(fixture("full-profile.json").getProfile());
		assertTrue(text.contains("9,260 / 10,000"));
		assertTrue(text.contains("Giga Chad"));
		assertTrue(text.contains("Skills"));
		assertTrue(text.contains("Combat"));
		assertTrue(text.contains("Boss Bravery"));
		assertTrue(text.contains("Raid Reputation"));
		assertTrue(text.contains("Quests"));
		assertTrue(text.contains("Start the Inferno cape grind"));
		assertTrue(text.contains("Fire Cape Acquired"));
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

	private static String render(ProfileResponse.Profile profile)
	{
		AtomicReference<String> rendered = new AtomicReference<>();
		onEdt(() -> {
			AccountPanel panel = new AccountPanel();
			panel.render(profile);
			rendered.set(String.join("\n", textOf(panel)));
		});
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
}
