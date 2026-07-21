package com.nextmove;

import java.util.List;
import java.util.Objects;

public final class QuestSnapshot
{
	private final int schemaVersion = 1;
	private final String capturedAt;
	private final Account account;
	private final List<QuestEntry> quests;
	private final Source source;
	private final List<String> completedRecommendationIds;

	public QuestSnapshot(
		String capturedAt,
		String displayName,
		List<QuestEntry> quests,
		String pluginVersion)
	{
		this(capturedAt, displayName, quests, pluginVersion, List.of());
	}

	private QuestSnapshot(
		String capturedAt,
		String displayName,
		List<QuestEntry> quests,
		String pluginVersion,
		List<String> completedRecommendationIds)
	{
		this.capturedAt = Objects.requireNonNull(capturedAt);
		this.account = new Account(displayName);
		this.quests = List.copyOf(Objects.requireNonNull(quests));
		this.source = new Source(pluginVersion);
		this.completedRecommendationIds = List.copyOf(
			Objects.requireNonNull(completedRecommendationIds));
	}

	public int getSchemaVersion()
	{
		return schemaVersion;
	}

	public String getCapturedAt()
	{
		return capturedAt;
	}

	public Account getAccount()
	{
		return account;
	}

	public List<QuestEntry> getQuests()
	{
		return quests;
	}

	public Source getSource()
	{
		return source;
	}

	public List<String> getCompletedRecommendationIds()
	{
		return completedRecommendationIds;
	}

	public QuestSnapshot withCompletedRecommendationIds(List<String> ids)
	{
		return new QuestSnapshot(
			capturedAt,
			account.getDisplayName(),
			quests,
			source.getPluginVersion(),
			ids);
	}

	public static final class Account
	{
		private final String displayName;

		private Account(String displayName)
		{
			this.displayName = Objects.requireNonNull(displayName);
		}

		public String getDisplayName()
		{
			return displayName;
		}
	}

	public static final class QuestEntry
	{
		private final String key;
		private final String name;
		private final String state;

		public QuestEntry(String key, String name, String state)
		{
			this.key = Objects.requireNonNull(key);
			this.name = Objects.requireNonNull(name);
			this.state = Objects.requireNonNull(state);
		}

		public String getKey()
		{
			return key;
		}

		public String getName()
		{
			return name;
		}

		public String getState()
		{
			return state;
		}
	}

	public static final class Source
	{
		private final String pluginVersion;

		private Source(String pluginVersion)
		{
			this.pluginVersion = Objects.requireNonNull(pluginVersion);
		}

		public String getPluginVersion()
		{
			return pluginVersion;
		}
	}
}
