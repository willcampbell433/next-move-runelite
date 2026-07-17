package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

public class BossesPanel extends JPanel
{
	public BossesPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
	}

	public void render(ProfileResponse.Profile profile)
	{
		removeAll();
		ProfileResponse.Bosses bosses = profile.getBosses();
		add(section("BOSS BRAVERY"));
		if (bosses.getScore() == null)
		{
			add(new JLabel("Boss Bravery unavailable"));
		}
		else
		{
			JLabel score = new JLabel("Boss Bravery " + bosses.getScore() + " / 100");
			score.setFont(FontManager.getRunescapeBoldFont());
			add(score);
			add(new JLabel(bosses.getTierLabel()));
			if (bosses.getNextTierScore() != null)
			{
				int points = Math.max(0, bosses.getNextTierScore() - bosses.getScore());
				add(new JLabel(points + (points == 1 ? " point" : " points")
					+ " to " + nextTierLabel(bosses.getNextTierScore())));
			}
		}

		add(gap(10));
		add(section("NEXT CHALLENGE"));
		ProfileResponse.RecommendedChallenge challenge = bosses.getRecommendedChallenge();
		if (challenge == null)
		{
			add(wrapped("No public boss challenge is ready"));
		}
		else
		{
			JLabel name = wrapped(challenge.getName());
			name.setFont(FontManager.getRunescapeBoldFont());
			add(name);
			add(wrapped(challenge.getRationale()));
		}

		add(gap(10));
		add(section("ATTEMPTED BOSSES"));
		if (bosses.getAttempted().isEmpty())
		{
			add(new JLabel("No public boss KC yet"));
		}
		else
		{
			for (ProfileResponse.AttemptedBoss boss : bosses.getAttempted())
			{
				add(wrapped(
					boss.getName()
						+ " · "
						+ NumberFormat.getIntegerInstance(Locale.US).format(boss.getKills())
						+ " KC · "
						+ boss.getLabel()));
			}
		}

		add(gap(10));
		add(section("TROPHIES"));
		if (bosses.getTrophies().isEmpty())
		{
			add(new JLabel("No public boss trophies yet"));
		}
		else
		{
			for (ProfileResponse.Trophy trophy : bosses.getTrophies())
			{
				add(new JLabel("• " + trophy.getLabel()));
			}
		}

		add(gap(10));
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		buttons.setOpaque(false);
		if (challenge != null)
		{
			JButton wiki = new JButton("Open Wiki guide");
			wiki.addActionListener(event -> LinkBrowser.browse(
				LinkFactory.wiki(challenge.getWikiTitle())));
			buttons.add(wiki);
		}
		JButton website = new JButton("Open full Boss Tracker");
		website.addActionListener(event -> LinkBrowser.browse(
			LinkFactory.account(profile.getUsername(), LinkFactory.View.BOSSES)));
		buttons.add(website);
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(buttons);

		revalidate();
		repaint();
	}

	private static String nextTierLabel(int score)
	{
		switch (score)
		{
			case 10:
				return "Left Lumbridge";
			case 25:
				return "Officially Has Balls";
			case 40:
				return "Certified Menace";
			case 60:
				return "Boss Goblin";
			case 80:
				return "Built Different";
			case 95:
				return "Absolutely Unwell";
			default:
				return "next tier";
		}
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
		JLabel label = new JLabel("<html><body style='width: 205px'>" + text + "</body></html>");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static Component gap(int height)
	{
		return Box.createRigidArea(new Dimension(0, height));
	}
}
