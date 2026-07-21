package com.nextmove.api;

import com.nextmove.api.ProfileResponse.Account;
import com.nextmove.api.ProfileResponse.AttemptedBoss;
import com.nextmove.api.ProfileResponse.Bosses;
import com.nextmove.api.ProfileResponse.Category;
import com.nextmove.api.ProfileResponse.Recommendation;
import com.nextmove.api.ProfileResponse.RecommendedChallenge;
import com.nextmove.api.ProfileResponse.ScoreCoverage;
import com.nextmove.api.ProfileResponse.Trophy;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class ProfileValidator
{
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9 _-]{1,12}");
	private static final Set<String> DATA_SOURCES = Set.of("HISCORES", "RUNELITE");
	private static final Set<String> QUEST_DATA = Set.of("MISSING", "AVAILABLE");
	private static final Set<String> ACCOUNT_TIERS = Set.of(
		"INCOMPLETE", "EARLY", "MID", "LATE", "ENDGAME");
	private static final Set<String> BOSS_TIERS = Set.of(
		"UNAVAILABLE", "COMBAT_DUMMY_ENTHUSIAST", "LEFT_LUMBRIDGE",
		"OFFICIALLY_HAS_BALLS", "CERTIFIED_MENACE", "BOSS_GOBLIN",
		"BUILT_DIFFERENT", "ABSOLUTELY_UNWELL");
	private static final Set<String> CATEGORY_IDS = Set.of(
		"SKILLS", "COMBAT", "BOSSES", "RAIDS", "QUESTS");
	private static final Set<String> RECOMMENDATION_CATEGORIES = Set.of(
		"BOSS", "QUEST", "SKILLING", "PVM", "UNLOCK");
	private static final Set<String> VIEWS = Set.of("coach", "bosses", "stats");
	private static final Set<String> TROPHY_ICONS = Set.of(
		"FIRE", "CAPSTONE", "RAID", "GENERIC");

	private ProfileValidator()
	{
	}

	public static void validate(ProfileResponse response)
	{
		require(response != null, "Response is required");
		require(response.getSchemaVersion() == 1, "Unsupported schema version");
		text("Generated timestamp", response.getGeneratedAt(), 40);
		try
		{
			Instant.parse(response.getGeneratedAt());
		}
		catch (DateTimeException exception)
		{
			throw new ProfileValidationException("Generated timestamp must be ISO-8601");
		}

		ProfileResponse.Profile profile = response.getProfile();
		require(profile != null, "Profile is required");
		text("Username", profile.getUsername(), 12);
		require(USERNAME.matcher(profile.getUsername()).matches(), "Username is invalid");
		member("Data source", profile.getDataSource(), DATA_SOURCES);
		member("Quest data", profile.getQuestData(), QUEST_DATA);
		require(
			("HISCORES".equals(profile.getDataSource()) && "MISSING".equals(profile.getQuestData()))
				|| ("RUNELITE".equals(profile.getDataSource()) && "AVAILABLE".equals(profile.getQuestData())),
			"Quest data does not match its source");
		validateAccount(profile.getAccount());
		if (profile.getRecommendation() != null)
		{
			validateRecommendation(profile.getRecommendation());
		}
		validateRecommendationDeck(profile);
		validateBosses(profile.getBosses());
	}

	private static void validateRecommendationDeck(ProfileResponse.Profile profile)
	{
		List<Recommendation> recommendations = profile.getRecommendations();
		if (recommendations == null)
		{
			return;
		}
		require(recommendations.size() <= 6,
			"Recommendations must contain at most six items");
		Set<String> seen = new HashSet<>();
		for (Recommendation recommendation : recommendations)
		{
			require(recommendation != null, "Recommendation is required");
			validateRecommendation(recommendation);
			require(seen.add(recommendation.getId()), "Recommendation ids must be distinct");
		}
		if (profile.getRecommendation() == null)
		{
			require(recommendations.isEmpty(),
				"Recommendation deck must be empty without a primary recommendation");
		}
		else
		{
			require(!recommendations.isEmpty()
					&& profile.getRecommendation().getId().equals(recommendations.get(0).getId()),
				"Recommendation deck must begin with the primary recommendation");
		}
	}

	private static void validateAccount(Account account)
	{
		require(account != null, "Account is required");
		nullableBounded("Account score", account.getScore(), 0, 10_000);
		require(account.getMaximumScore() == 10_000, "Unexpected account maximum");
		ScoreCoverage coverage = account.getScoreCoverage();
		require(coverage != null, "Score coverage is required");
		bounded("Available weighted signals", coverage.getAvailableWeightedSignals(), 0, 3);
		require(coverage.getTotalWeightedSignals() == 3, "Total weighted signals must be 3");
		bounded("Available display signals", coverage.getAvailableDisplaySignals(), 0, 5);
		require(coverage.getTotalDisplaySignals() == 5, "Total display signals must be 5");
		member("Account tier", account.getTierId(), ACCOUNT_TIERS);
		text("Account tier label", account.getTierLabel(), 40);
		text("Account title", account.getTitle(), 80);
		text("Account verdict", account.getVerdict(), 280);
		nullableBounded("Account next tier score", account.getNextTierScore(), 0, 10_000);

		List<Category> categories = account.getCategories();
		require(categories != null && categories.size() == 5, "Expected five display categories");
		Set<String> seen = new HashSet<>();
		for (Category category : categories)
		{
			require(category != null, "Category is required");
			member("Category id", category.getId(), CATEGORY_IDS);
			require(seen.add(category.getId()), "Category ids must be distinct");
			text("Category label", category.getLabel(), 40);
			member("Category status", category.getStatus(), Set.of("AVAILABLE", "UNAVAILABLE"));
			require(category.getMaximumScore() == 100, "Category maximum score must be 100");
			if ("UNAVAILABLE".equals(category.getStatus()))
			{
				require(category.getScore() == null, "Unavailable category score must be null");
			}
			else
			{
				require(category.getScore() != null, "Available category score is required");
				bounded("Category score", category.getScore(), 0, 100);
			}
		}
		require(seen.equals(CATEGORY_IDS), "Expected all five category ids");
	}

	private static void validateRecommendation(Recommendation recommendation)
	{
		text("Recommendation id", recommendation.getId(), 120);
		member("Recommendation category", recommendation.getCategory(), RECOMMENDATION_CATEGORIES);
		text("Recommendation title", recommendation.getTitle(), 80);
		text("Recommendation rationale", recommendation.getRationale(), 280);
		List<String> evidence = recommendation.getEvidence();
		require(evidence != null && evidence.size() <= 4,
			"Recommendation evidence must contain at most four items");
		for (String item : evidence)
		{
			text("Recommendation evidence", item, 160);
		}
		text("Recommendation unlock", recommendation.getUnlock(), 160);
		member("Next Move view", recommendation.getNextMoveView(), VIEWS);
		text("Wiki title", recommendation.getWikiTitle(), 120);
	}

	private static void validateBosses(Bosses bosses)
	{
		require(bosses != null, "Boss summary is required");
		nullableBounded("Boss score", bosses.getScore(), 0, 100);
		require(bosses.getMaximumScore() == 100, "Boss maximum score must be 100");
		member("Boss tier", bosses.getTierId(), BOSS_TIERS);
		if (bosses.getTierLabel() != null)
		{
			text("Boss tier label", bosses.getTierLabel(), 80);
		}
		nullableBounded("Boss next tier score", bosses.getNextTierScore(), 0, 100);
		if (bosses.getRecommendedChallenge() != null)
		{
			RecommendedChallenge challenge = bosses.getRecommendedChallenge();
			text("Recommended challenge id", challenge.getId(), 80);
			text("Recommended challenge name", challenge.getName(), 80);
			text("Recommended challenge rationale", challenge.getRationale(), 280);
			text("Recommended challenge Wiki title", challenge.getWikiTitle(), 120);
		}

		List<AttemptedBoss> attempted = bosses.getAttempted();
		require(attempted != null && attempted.size() <= 64,
			"Attempted bosses must contain at most 64 entries");
		for (AttemptedBoss boss : attempted)
		{
			require(boss != null, "Attempted boss is required");
			text("Attempted boss id", boss.getId(), 80);
			text("Attempted boss name", boss.getName(), 80);
			bounded("Attempted boss kills", boss.getKills(), 1, 2_000_000_000);
			text("Attempted boss label", boss.getLabel(), 120);
		}

		List<Trophy> trophies = bosses.getTrophies();
		require(trophies != null && trophies.size() <= 6,
			"Trophies must contain at most six entries");
		for (Trophy trophy : trophies)
		{
			require(trophy != null, "Trophy is required");
			text("Trophy id", trophy.getId(), 80);
			text("Trophy label", trophy.getLabel(), 100);
			member("Trophy icon", trophy.getIconKey(), TROPHY_ICONS);
		}
	}

	private static void text(String name, String value, int maximum)
	{
		require(value != null && !value.trim().isEmpty() && value.length() <= maximum,
			name + " must contain 1 to " + maximum + " characters");
	}

	private static void member(String name, String value, Set<String> allowed)
	{
		require(value != null && allowed.contains(value), name + " is not supported");
	}

	private static void nullableBounded(
		String name,
		Integer value,
		int minimum,
		int maximum)
	{
		if (value != null)
		{
			bounded(name, value, minimum, maximum);
		}
	}

	private static void bounded(String name, int value, int minimum, int maximum)
	{
		if (value < minimum || value > maximum)
		{
			throw new ProfileValidationException(name + " is outside its declared bounds");
		}
	}

	private static void require(boolean condition, String message)
	{
		if (!condition)
		{
			throw new ProfileValidationException(message);
		}
	}
}
