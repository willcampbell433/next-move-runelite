package com.nextmove.ui;

import com.nextmove.api.ProfileResponse;
import com.nextmove.completion.CompletedRecommendation;
import com.nextmove.links.LinkFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
	private String focusedGoalId;
	private boolean editable;
	private boolean completedOpen;
	private List<CompletedRecommendation> completedRecommendations = List.of();
	private Consumer<String> focusChanged = ignored -> { };
	private Consumer<ProfileResponse.Recommendation> markDone = ignored -> { };
	private Consumer<String> restore = ignored -> { };

	public CoachPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	public void render(ProfileResponse.Profile profile)
	{
		render(profile, null, ignored -> { });
	}

	public void render(
		ProfileResponse.Profile profile,
		String focusedGoalId,
		Consumer<String> focusChanged)
	{
		render(
			profile,
			focusedGoalId,
			false,
			List.of(),
			focusChanged,
			ignored -> { },
			ignored -> { });
	}

	public void render(
		ProfileResponse.Profile profile,
		String focusedGoalId,
		boolean editable,
		List<CompletedRecommendation> completedRecommendations,
		Consumer<String> focusChanged,
		Consumer<ProfileResponse.Recommendation> markDone,
		Consumer<String> restore)
	{
		this.profile = profile;
		this.completedRecommendations = new ArrayList<>(completedRecommendations);
		this.recommendations = recommendationDeck(profile).stream()
			.filter(recommendation -> this.completedRecommendations.stream()
				.noneMatch(completed -> completed.getId().equals(recommendation.getId())))
			.collect(Collectors.toCollection(ArrayList::new));
		this.selectedFilter = Filter.ALL;
		this.focusedGoalId = focusedGoalId;
		this.editable = editable;
		this.completedOpen = false;
		this.focusChanged = focusChanged;
		this.markDone = markDone;
		this.restore = restore;
		rebuild();
	}

	private void rebuild()
	{
		removeAll();
		ProfileResponse.Recommendation focused = focusedRecommendation();
		if (focused != null)
		{
			add(section("TRACKING THIS GOAL"));
			add(gap(8));
			add(recommendationCard(focused, false));
			add(gap(2));
			add(wrapped("Focused for this RuneLite session."));
			add(gap(8));
			JButton browse = new JButton("Browse other goals");
			browse.addActionListener(event -> {
				focusedGoalId = null;
				focusChanged.accept(null);
				rebuild();
			});
			add(SidebarUi.buttonStack(browse));
			addCompletedSection();
			revalidate();
			repaint();
			return;
		}

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
				add(recommendationCard(recommendation, true));
			}
		}

		addCompletedSection();
		add(gap(12));
		add(wrapped(editable
			? "Completed goals are stored locally for this character. Full goal tracking and history stay on the Next Move website."
			: "Browse every ranked idea here. Goal tracking and completion controls are available only for your logged-in character."));
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

	private JPanel recommendationCard(
		ProfileResponse.Recommendation recommendation,
		boolean showTrackAction)
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
		List<JButton> actions = new ArrayList<>();
		if (showTrackAction)
		{
			JButton track = new JButton("Track this goal");
			track.addActionListener(event -> {
				focusedGoalId = recommendation.getId();
				focusChanged.accept(focusedGoalId);
				rebuild();
			});
			actions.add(track);
		}
		if (editable)
		{
			JButton done = new JButton("Mark done");
			done.addActionListener(event -> complete(recommendation));
			actions.add(done);
		}
		actions.add(wiki);
		actions.add(websiteButton(recommendation));
		card.add(SidebarUi.buttonStack(actions.toArray(new JButton[0])));
		card.add(gap(10));
		JSeparator divider = new JSeparator();
		divider.setAlignmentX(Component.LEFT_ALIGNMENT);
		divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		card.add(divider);
		return card;
	}

	private void complete(ProfileResponse.Recommendation recommendation)
	{
		markDone.accept(recommendation);
		recommendations = recommendations.stream()
			.filter(candidate -> !recommendation.getId().equals(candidate.getId()))
			.collect(Collectors.toList());
		List<CompletedRecommendation> updated = completedRecommendations.stream()
			.filter(candidate -> !recommendation.getId().equals(candidate.getId()))
			.collect(Collectors.toCollection(ArrayList::new));
		updated.add(0, new CompletedRecommendation(
			recommendation.getId(),
			recommendation.getTitle(),
			recommendation.getCategory(),
			Instant.now().toString()));
		completedRecommendations = updated;
		if (recommendation.getId().equals(focusedGoalId))
		{
			focusedGoalId = null;
			focusChanged.accept(null);
		}
		rebuild();
	}

	private void addCompletedSection()
	{
		if (!editable || completedRecommendations.isEmpty())
		{
			return;
		}
		add(gap(12));
		JButton toggle = new JButton("Completed (" + completedRecommendations.size() + ")");
		toggle.addActionListener(event -> {
			completedOpen = !completedOpen;
			rebuild();
		});
		add(SidebarUi.buttonStack(toggle));
		if (!completedOpen)
		{
			return;
		}
		for (CompletedRecommendation completed : new ArrayList<>(completedRecommendations))
		{
			add(gap(8));
			add(section(categoryLabel(completed.getCategory())));
			add(wrapped(completed.getTitle()));
			add(wrapped("Completed " + displayDate(completed.getCompletedAt())));
			JButton restoreButton = new JButton("Restore");
			restoreButton.addActionListener(event -> restore(completed));
			add(SidebarUi.buttonStack(restoreButton));
		}
	}

	private void restore(CompletedRecommendation completed)
	{
		restore.accept(completed.getId());
		completedRecommendations = completedRecommendations.stream()
			.filter(candidate -> !completed.getId().equals(candidate.getId()))
			.collect(Collectors.toList());
		if (completedRecommendations.isEmpty())
		{
			completedOpen = false;
		}
		rebuild();
	}

	private static String displayDate(String completedAt)
	{
		return completedAt != null && completedAt.length() >= 10
			? completedAt.substring(0, 10)
			: completedAt;
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

	private ProfileResponse.Recommendation focusedRecommendation()
	{
		if (focusedGoalId == null)
		{
			return null;
		}
		return recommendations.stream()
			.filter(recommendation -> focusedGoalId.equals(recommendation.getId()))
			.findFirst()
			.orElse(null);
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
