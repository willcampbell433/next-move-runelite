package com.nextmove.ui;

import com.google.gson.Gson;
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

public class BossesPanelTest
{
	@Test
	public void rendersBossBraveryChallengeAttemptedKcAndTrophies()
	{
		String text = render(fixture("full-profile.json").getProfile());
		assertTrue(text.contains("Boss Bravery 70 / 100"));
		assertTrue(text.contains("Boss Goblin"));
		assertTrue(text.contains("10 points to Built Different"));
		assertTrue(text.contains("The Inferno"));
		assertTrue(text.indexOf("Kraken · 1,683 KC") < text.indexOf("TzTok-Jad · 7 KC"));
		assertTrue(text.contains("Fire Cape Acquired"));
		assertTrue(text.contains("Open full Boss Tracker"));
		assertTrue(text.contains("Open Wiki guide"));
		assertFalse(text.toLowerCase().contains("gear"));
		assertFalse(text.toLowerCase().contains("inventory"));
	}

	@Test
	public void rendersUnavailableBossDataWithoutInventingAZero()
	{
		String text = render(fixture("partial-profile.json").getProfile());
		assertTrue(text.contains("Boss Bravery unavailable"));
		assertFalse(text.contains("Boss Bravery 0"));
	}

	private static String render(ProfileResponse.Profile profile)
	{
		AtomicReference<String> rendered = new AtomicReference<>();
		onEdt(() -> {
			BossesPanel panel = new BossesPanel();
			panel.render(profile);
			rendered.set(String.join("\n", textOf(panel)));
		});
		return rendered.get();
	}

	private static ProfileResponse fixture(String name)
	{
		try (InputStream stream = BossesPanelTest.class.getResourceAsStream("/fixtures/" + name))
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
