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

public class CoachPanelTest
{
	@Test
	public void rendersTheReadOnlyRecommendationAndEvidence()
	{
		String text = render(fixture("full-profile.json").getProfile());
		assertTrue(text.contains("Start the Inferno cape grind"));
		assertTrue(text.contains("The missing account capstone is Zuk."));
		assertTrue(text.contains("7 TzTok-Jad KC proves the foundation."));
		assertTrue(text.contains("First Inferno completion"));
		assertTrue(text.contains("Open Wiki guide"));
		assertTrue(text.contains("Continue on Next Move"));
		assertFalse(text.contains("\nDone\n"));
		assertFalse(text.contains("\nNot today\n"));
		assertFalse(text.contains("\nSave\n"));
		assertFalse(text.contains("\nTrack\n"));
	}

	@Test
	public void rendersAUsefulEmptyState()
	{
		String text = render(fixture("partial-profile.json").getProfile());
		assertTrue(text.contains("No public recommendation is ready"));
		assertTrue(text.contains("Continue on Next Move"));
	}

	private static String render(ProfileResponse.Profile profile)
	{
		AtomicReference<String> rendered = new AtomicReference<>();
		onEdt(() -> {
			CoachPanel panel = new CoachPanel();
			panel.render(profile);
			rendered.set(String.join("\n", textOf(panel)));
		});
		return rendered.get();
	}

	private static ProfileResponse fixture(String name)
	{
		try (InputStream stream = CoachPanelTest.class.getResourceAsStream("/fixtures/" + name))
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
