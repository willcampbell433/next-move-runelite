package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

public class CoachPanel extends JPanel
{
	private enum Filter
	{
		ALL("All", null, "ideas"),
		SKILLING("Skilling", "SKILLING", "Skilling ideas"),
		BOSSES("Bosses", "BOSS", "Boss ideas"),
		QUESTS("Quests", "QUEST", "Quest ideas"),
		PVM("PvM", "PVM", "PvM ideas"),
		UNLOCKS("Unlocks", "UNLOCK", "Unlock ideas");

		private final String label;
		private final String category;
		private final String emptyLabel;

		Filter(String label, String category, String emptyLabel)
		{
			this.label = label;
			this.category = category;
			this.emptyLabel = emptyLabel;
		}
	}

	private ProfileResponse.Profile profile;
	private List<ProfileResponse.Recommendation> recommendations = List.of();
	private Filter selectedFilter = Filter.ALL;

	public CoachPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	public void render(ProfileResponse.Profile profile)
	{
		this.profile = profile;
		this.recommendations = recommendationDeck(profile);
		this.selectedFilter = Filter.ALL;
		rebuild();
	}

	private void rebuild()
	{
		removeAll();
		add(section("RECOMMENDATIONS"));
		add(gap(5));
		add(filterPanel());
		add(gap(8));

		List<ProfileResponse.Recommendation> filtered = recommendations.stream()
			.filter(this::matchesFilter)
			.collect(Collectors.toList());
		add(section("SHOWING " + filtered.size() + " OF " + recommendations.size() + " IDEAS"));

		if (recommendations.isEmpty())
		{
			add(gap(8));
			add(wrapped("No public recommendation is ready"));
			add(gap(10));
			add(SidebarUi.buttonStack(websiteButton(null)));
		}
		else if (filtered.isEmpty())
		{
			add(gap(8));
			add(wrapped("No " + selectedFilter.emptyLabel + " are ready for this account."));
			add(gap(8));
			JButton showAll = new JButton("Show all ideas");
			showAll.addActionListener(event -> selectFilter(Filter.ALL));
			add(SidebarUi.buttonStack(showAll));
		}
		else
		{
			for (ProfileResponse.Recommendation recommendation : filtered)
			{
				add(gap(12));
				add(recommendationCard(recommendation));
			}
		}

		add(gap(12));
		add(wrapped("Browse every ranked idea here. Done, goal tracking, and full history stay on the Next Move website."));
		revalidate();
		repaint();
	}

	private JPanel filterPanel()
	{
		JPanel panel = new JPanel(new GridLayout(2, 3, 4, 4));
		panel.setOpaque(false);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		for (Filter filter : Filter.values())
		{
			JButton button = new JButton(filter.label);
			button.setMargin(new Insets(2, 2, 2, 2));
			button.setEnabled(filter != selectedFilter);
			button.addActionListener(event -> selectFilter(filter));
			panel.add(button);
		}
		return panel;
	}

	private JPanel recommendationCard(ProfileResponse.Recommendation recommendation)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setOpaque(false);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		card.add(section(categoryLabel(recommendation.getCategory())));
		card.add(gap(3));
		JLabel title = wrapped(recommendation.getTitle());
		title.setFont(FontManager.getRunescapeBoldFont());
		card.add(title);
		card.add(gap(5));
		card.add(wrapped(recommendation.getRationale()));

		if (!recommendation.getEvidence().isEmpty())
		{
			card.add(gap(8));
			card.add(section("WHY THIS FITS"));
			for (String evidence : recommendation.getEvidence())
			{
				card.add(wrapped("- " + evidence));
			}
		}

		card.add(gap(8));
		card.add(section("CHECKPOINT"));
		card.add(wrapped(recommendation.getUnlock()));
		card.add(gap(8));

		JButton wiki = new JButton("Open Wiki guide");
		wiki.addActionListener(event -> LinkBrowser.browse(
			LinkFactory.wiki(recommendation.getWikiTitle())));
		card.add(SidebarUi.buttonStack(wiki, websiteButton(recommendation)));
		card.add(gap(10));
		JSeparator divider = new JSeparator();
		divider.setAlignmentX(Component.LEFT_ALIGNMENT);
		divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		card.add(divider);
		return card;
	}

	private JButton websiteButton(ProfileResponse.Recommendation recommendation)
	{
		JButton website = new JButton("Continue on Next Move");
		website.addActionListener(event -> LinkBrowser.browse(LinkFactory.account(
			profile.getUsername(),
			recommendation == null
				? LinkFactory.View.COACH
				: viewFor(recommendation.getNextMoveView()))));
		return website;
	}

	private boolean matchesFilter(ProfileResponse.Recommendation recommendation)
	{
		return selectedFilter.category == null
			|| selectedFilter.category.equals(recommendation.getCategory());
	}

	private void selectFilter(Filter filter)
	{
		selectedFilter = filter;
		rebuild();
	}

	private static List<ProfileResponse.Recommendation> recommendationDeck(
		ProfileResponse.Profile profile)
	{
		List<ProfileResponse.Recommendation> deck = profile.getRecommendations();
		if (deck == null || deck.isEmpty())
		{
			return profile.getRecommendation() == null
				? List.of()
				: List.of(profile.getRecommendation());
		}
		return deck;
	}

	private static String categoryLabel(String category)
	{
		if ("BOSS".equals(category))
		{
			return "BOSSING";
		}
		if ("PVM".equals(category))
		{
			return "PVM";
		}
		if ("QUEST".equals(category))
		{
			return "QUEST";
		}
		if ("UNLOCK".equals(category))
		{
			return "UNLOCK";
		}
		return "SKILLING";
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
