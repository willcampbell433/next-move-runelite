package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

public class AccountPanel extends JPanel
{
	public AccountPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
	}

	public void render(ProfileResponse.Profile profile)
	{
		removeAll();
		ProfileResponse.Account account = profile.getAccount();

		add(section("ACCOUNT POWER"));
		String accountScore = account.getScore() == null
			? "Unavailable"
			: String.format(
				Locale.US,
				"%.1f / 100",
				account.getMaximumScore() <= 0
					? 0.0
					: account.getScore() * 100.0 / account.getMaximumScore());
		JLabel score = new JLabel(accountScore);
		score.setFont(FontManager.getRunescapeBoldFont());
		add(score);
		add(wrapped(account.getTierLabel() + " · " + account.getTitle()));
		add(wrapped(account.getVerdict()));

		if (account.getScoreCoverage().getAvailableWeightedSignals()
			< account.getScoreCoverage().getTotalWeightedSignals()
			|| account.getScoreCoverage().getAvailableDisplaySignals()
			< account.getScoreCoverage().getTotalDisplaySignals())
		{
			add(new JLabel("Partial public score"));
		}

		add(gap(10));
		add(section("NEXT MOVE"));
		ProfileResponse.Recommendation recommendation = profile.getRecommendation();
		add(wrapped(recommendation == null
			? "No public recommendation is ready"
			: recommendation.getTitle()));

		add(gap(10));
		for (ProfileResponse.Category category : account.getCategories())
		{
			ScoreBar bar = new ScoreBar(
				displayLabel(category.getId(), category.getLabel()),
				category.getScore(),
				category.getMaximumScore());
			bar.setAlignmentX(Component.LEFT_ALIGNMENT);
			bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, bar.getPreferredSize().height));
			add(bar);
			add(gap(5));
		}

		add(gap(5));
		add(section("TROPHIES"));
		if (profile.getBosses().getTrophies().isEmpty())
		{
			add(new JLabel("No public boss trophies yet"));
		}
		else
		{
			int shown = Math.min(4, profile.getBosses().getTrophies().size());
			for (int index = 0; index < shown; index += 1)
			{
				add(wrapped("- " + profile.getBosses().getTrophies().get(index).getLabel()));
			}
			int remaining = profile.getBosses().getTrophies().size() - shown;
			if (remaining > 0)
			{
				add(wrapped("+ " + remaining + " more on the website"));
			}
		}

		add(gap(10));
		JButton website = new JButton("How is this score calculated?");
		website.setAlignmentX(Component.LEFT_ALIGNMENT);
		website.addActionListener(event -> LinkBrowser.browse(
			LinkFactory.account(profile.getUsername(), LinkFactory.View.STATS)));
		add(website);

		revalidate();
		repaint();
	}

	private static String displayLabel(String id, String fallback)
	{
		if ("BOSSES".equals(id))
		{
			return "Boss Bravery";
		}
		if ("RAIDS".equals(id))
		{
			return "Raid Reputation";
		}
		return fallback;
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
