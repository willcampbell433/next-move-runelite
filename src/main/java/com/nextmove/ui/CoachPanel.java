package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

public class CoachPanel extends JPanel
{
	public CoachPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
	}

	public void render(ProfileResponse.Profile profile)
	{
		removeAll();
		add(section("YOUR NEXT MOVE"));
		ProfileResponse.Recommendation recommendation = profile.getRecommendation();
		if (recommendation == null)
		{
			add(wrapped("No public recommendation is ready"));
		}
		else
		{
			JLabel title = wrapped(recommendation.getTitle());
			title.setFont(FontManager.getRunescapeBoldFont());
			add(title);
			add(gap(6));
			add(wrapped(recommendation.getRationale()));

			if (!recommendation.getEvidence().isEmpty())
			{
				add(gap(10));
				add(section("WHY THIS FITS"));
				for (String evidence : recommendation.getEvidence())
				{
					add(wrapped("- " + evidence));
				}
			}

			add(gap(10));
			add(section("CHECKPOINT"));
			add(wrapped(recommendation.getUnlock()));
		}

		add(gap(10));
		JButton website = new JButton("Continue on Next Move");
		website.addActionListener(event -> LinkBrowser.browse(LinkFactory.account(
			profile.getUsername(),
			recommendation == null
				? LinkFactory.View.COACH
				: viewFor(recommendation.getNextMoveView()))));
		if (recommendation != null)
		{
			JButton wiki = new JButton("Open Wiki guide");
			wiki.addActionListener(event -> LinkBrowser.browse(
				LinkFactory.wiki(recommendation.getWikiTitle())));
			add(SidebarUi.buttonStack(wiki, website));
		}
		else
		{
			add(SidebarUi.buttonStack(website));
		}

		add(gap(12));
		add(wrapped(
			"Tracking, passing, and completing recommendations stay on the Next Move website in this version."));

		revalidate();
		repaint();
	}

	private static LinkFactory.View viewFor(String value)
	{
		if ("bosses".equals(value))
		{
			return LinkFactory.View.BOSSES;
		}
		if ("stats".equals(value))
		{
			return LinkFactory.View.STATS;
		}
		return LinkFactory.View.COACH;
	}

	private static JLabel section(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeBoldFont());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static JLabel wrapped(String text)
	{
		return SidebarUi.wrapped(text);
	}

	private static Component gap(int height)
	{
		return Box.createRigidArea(new Dimension(0, height));
	}
}
