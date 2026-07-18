package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
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
		render(profile, profile.getRecommendation(), List.of(), null, ignored -> { });
	}

	void render(
		ProfileResponse.Profile profile,
		ProfileResponse.Recommendation recommendation,
		List<ProfileResponse.Recommendation> recommendations,
		Runnable onNext,
		Consumer<ProfileResponse.Recommendation> onSelect)
	{
		removeAll();
		add(section("YOUR NEXT MOVE"));
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
			if (onNext == null)
			{
				add(SidebarUi.buttonStack(wiki, website));
			}
			else
			{
				JButton next = new JButton("Next idea");
				next.addActionListener(event -> onNext.run());
				add(SidebarUi.buttonStack(wiki, next, website));
			}
		}
		else
		{
			add(SidebarUi.buttonStack(website));
		}

		if (recommendation != null && recommendations.size() > 1)
		{
			add(gap(14));
			add(section("OTHER IDEAS"));
			for (ProfileResponse.Recommendation alternative : recommendations)
			{
				if (alternative.getId().equals(recommendation.getId()))
				{
					continue;
				}
				JButton select = new JButton(alternative.getTitle());
				select.addActionListener(event -> onSelect.accept(alternative));
				add(gap(4));
				add(SidebarUi.buttonStack(select));
			}
		}

		add(gap(12));
		add(wrapped(recommendations.size() > 1
			? "Browse ideas here. Done, goal tracking, and full history stay on the Next Move website."
			: "Tracking and completing recommendations stay on the Next Move website."));

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
