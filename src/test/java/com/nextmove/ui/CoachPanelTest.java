package com.nextmove.ui;

import com.google.gson.Gson;
import com.nextmove.api.ProfileResponse;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoachPanelTest
{
	@Test
	public void rendersTheReadOnlyRecommendationAndEvidence()
	{
		CoachPanel panel = renderPanel(fixture("full-profile.json").getProfile());
		String text = text(panel);
		assertTrue(text.contains("Start the Inferno cape grind"));
		assertTrue(text.contains("The missing account capstone is Zuk."));
		assertTrue(text.contains("7 TzTok-Jad KC proves the foundation."));
		assertTrue(text.contains("First Inferno completion"));
		assertTrue(text.contains("Open Wiki guide"));
		assertTrue(text.contains("Continue on Next Move"));
		assertTrue(text.contains("Push Ranged to 100"));
		assertTrue(text.contains("A short mastery checkpoint keeps the account moving."));
		assertTrue(text.contains("The next Ranged mastery checkpoint"));
		assertTrue(text.contains("SHOWING 2 OF 2 IDEAS"));
		assertFalse(text.contains("Next idea"));
		assertFalse(text.contains("OTHER IDEAS"));
		assertFalse(text.contains("\nDone\n"));
		assertFalse(text.contains("\nNot today\n"));
		assertFalse(text.contains("\nSave\n"));
		assertFalse(text.contains("\nTrack\n"));
		assertFalse(text.contains("•"));
		assertFalse(text.contains("width: 205px"));
		assertFalse(hasLayout(panel, FlowLayout.class));
		assertEquals(Component.LEFT_ALIGNMENT, panel.getAlignmentX(), 0.0f);
	}

	@Test
	public void filtersTheFullFeedByRecommendationCategory()
	{
		CoachPanel panel = renderPanel(fixture("full-profile.json").getProfile());
		String initial = text(panel);
		for (String filter : new String[] {"All", "Skilling", "Bosses", "Quests", "PvM", "Unlocks"})
		{
			assertTrue(initial.contains(filter));
		}

		click(panel, "Skilling");
		String skilling = text(panel);
		assertTrue(skilling.contains("Push Ranged to 100"));
		assertFalse(skilling.contains("Start the Inferno cape grind"));
		assertTrue(skilling.contains("SHOWING 1 OF 2 IDEAS"));

		click(panel, "Bosses");
		String bosses = text(panel);
		assertTrue(bosses.contains("Start the Inferno cape grind"));
		assertFalse(bosses.contains("Push Ranged to 100"));
	}

	@Test
	public void rendersAUsefulEmptyFilterState()
	{
		CoachPanel panel = renderPanel(fixture("full-profile.json").getProfile());
		click(panel, "Quests");

		String text = text(panel);
		assertTrue(text.contains("No Quest ideas are ready for this account."));
		assertTrue(text.contains("Show all ideas"));
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
		return text(renderPanel(profile));
	}

	private static CoachPanel renderPanel(ProfileResponse.Profile profile)
	{
		AtomicReference<CoachPanel> rendered = new AtomicReference<>();
		onEdt(() -> {
			CoachPanel panel = new CoachPanel();
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

	private static void click(Component component, String label)
	{
		onEdt(() -> {
			AbstractButton button = button(component, label);
			if (button == null)
			{
				throw new AssertionError("Missing button " + label);
			}
			button.doClick();
		});
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

	private static boolean hasLayout(Component component, Class<?> layoutType)
	{
		if (component instanceof Container)
		{
			Container container = (Container) component;
			if (layoutType.isInstance(container.getLayout()))
			{
				return true;
			}
			for (Component child : container.getComponents())
			{
				if (hasLayout(child, layoutType))
				{
					return true;
				}
			}
		}
		return false;
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
