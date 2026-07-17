package com.nextmove.api;

import java.util.List;
import lombok.Getter;

@Getter
public class ProfileResponse
{
	private int schemaVersion;
	private String generatedAt;
	private Profile profile;

	// Test support for validator boundary cases.
	void setSchemaVersionForTest(int schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}

	@Getter
	public static class Profile
	{
		private String username;
		private String dataSource;
		private String questData;
		private Account account;
		private Recommendation recommendation;
		private Bosses bosses;
	}

	@Getter
	public static class ScoreCoverage
	{
		private int availableWeightedSignals;
		private int totalWeightedSignals;
		private int availableDisplaySignals;
		private int totalDisplaySignals;
	}

	@Getter
	public static class Account
	{
		private Integer score;
		private int maximumScore;
		private ScoreCoverage scoreCoverage;
		private String tierId;
		private String tierLabel;
		private String title;
		private String verdict;
		private Integer nextTierScore;
		private List<Category> categories;

		// Test support for validator boundary cases.
		void setScoreForTest(Integer score)
		{
			this.score = score;
		}
	}

	@Getter
	public static class Category
	{
		private String id;
		private String label;
		private String status;
		private Integer score;
		private int maximumScore;
	}

	@Getter
	public static class Recommendation
	{
		private String id;
		private String category;
		private String title;
		private String rationale;
		private List<String> evidence;
		private String unlock;
		private String nextMoveView;
		private String wikiTitle;
	}

	@Getter
	public static class RecommendedChallenge
	{
		private String id;
		private String name;
		private String rationale;
		private String wikiTitle;
	}

	@Getter
	public static class AttemptedBoss
	{
		private String id;
		private String name;
		private int kills;
		private String label;
	}

	@Getter
	public static class Trophy
	{
		private String id;
		private String label;
		private String iconKey;
	}

	@Getter
	public static class Bosses
	{
		private Integer score;
		private int maximumScore;
		private String tierId;
		private String tierLabel;
		private Integer nextTierScore;
		private RecommendedChallenge recommendedChallenge;
		private List<AttemptedBoss> attempted;
		private List<Trophy> trophies;
	}
}
