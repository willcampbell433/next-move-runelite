package com.nextmove.completion;

import com.google.gson.Gson;
import com.nextmove.api.ProfileResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.runelite.client.config.ConfigManager;

public final class CompletionStore implements CompletionRepository
{
	private static final String CONFIG_GROUP = "next-move";
	private static final String KEY_PREFIX = "completedRecommendations.";
	private static final int MAX_ENTRIES = 200;
	private static final int MAX_ID_LENGTH = 120;
	private static final int MAX_TITLE_LENGTH = 100;
	private static final int MAX_CATEGORY_LENGTH = 20;

	interface Settings
	{
		String get(String key);

		void set(String key, String value);

		void unset(String key);
	}

	private final Settings settings;
	private final Gson gson;

	public CompletionStore(ConfigManager configManager, Gson gson)
	{
		this(new RuneLiteSettings(configManager), gson);
	}

	CompletionStore(Settings settings, Gson gson)
	{
		this.settings = Objects.requireNonNull(settings);
		this.gson = Objects.requireNonNull(gson);
	}

	@Override
	public synchronized List<CompletedRecommendation> load(String username)
	{
		String accountKey = accountKey(username);
		if (accountKey == null)
		{
			return List.of();
		}
		String value = settings.get(KEY_PREFIX + accountKey);
		if (value == null || value.trim().isEmpty())
		{
			return List.of();
		}
		try
		{
			CompletionLedger ledger = gson.fromJson(value, CompletionLedger.class);
			if (ledger == null || ledger.getVersion() != CompletionLedger.VERSION
				|| ledger.getEntries() == null)
			{
				return List.of();
			}
			Map<String, CompletedRecommendation> valid = new LinkedHashMap<>();
			for (CompletedRecommendation entry : ledger.getEntries())
			{
				if (!valid(entry) || valid.containsKey(entry.getId()))
				{
					continue;
				}
				valid.put(entry.getId(), entry);
				if (valid.size() == MAX_ENTRIES)
				{
					break;
				}
			}
			return List.copyOf(valid.values());
		}
		catch (RuntimeException exception)
		{
			return List.of();
		}
	}

	@Override
	public synchronized List<String> completedIds(String username)
	{
		return load(username).stream()
			.map(CompletedRecommendation::getId)
			.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public synchronized void markDone(
		String username,
		ProfileResponse.Recommendation recommendation,
		Instant completedAt)
	{
		Objects.requireNonNull(recommendation);
		CompletedRecommendation entry = new CompletedRecommendation(
			recommendation.getId(),
			recommendation.getTitle(),
			recommendation.getCategory(),
			Objects.requireNonNull(completedAt).toString());
		if (!valid(entry))
		{
			throw new IllegalArgumentException("Invalid completed recommendation");
		}

		List<CompletedRecommendation> entries = new ArrayList<>();
		entries.add(entry);
		load(username).stream()
			.filter(existing -> !entry.getId().equals(existing.getId()))
			.limit(MAX_ENTRIES - 1L)
			.forEach(entries::add);
		save(username, entries);
	}

	@Override
	public synchronized void restore(String username, String recommendationId)
	{
		if (recommendationId == null)
		{
			return;
		}
		List<CompletedRecommendation> remaining = load(username).stream()
			.filter(entry -> !recommendationId.equals(entry.getId()))
			.collect(Collectors.toList());
		save(username, remaining);
	}

	private void save(String username, List<CompletedRecommendation> entries)
	{
		String accountKey = accountKey(username);
		if (accountKey == null)
		{
			throw new IllegalArgumentException("Invalid account name");
		}
		String key = KEY_PREFIX + accountKey;
		if (entries.isEmpty())
		{
			settings.unset(key);
			return;
		}
		settings.set(key, gson.toJson(new CompletionLedger(entries)));
	}

	private static boolean valid(CompletedRecommendation entry)
	{
		if (entry == null
			|| !bounded(entry.getId(), MAX_ID_LENGTH)
			|| !bounded(entry.getTitle(), MAX_TITLE_LENGTH)
			|| !bounded(entry.getCategory(), MAX_CATEGORY_LENGTH)
			|| entry.getCompletedAt() == null)
		{
			return false;
		}
		try
		{
			Instant.parse(entry.getCompletedAt());
			return true;
		}
		catch (DateTimeParseException exception)
		{
			return false;
		}
	}

	private static boolean bounded(String value, int maximum)
	{
		return value != null && !value.trim().isEmpty() && value.length() <= maximum;
	}

	private static String accountKey(String username)
	{
		if (username == null || username.trim().isEmpty())
		{
			return null;
		}
		return username.trim()
			.toLowerCase(Locale.ROOT)
			.replaceAll("\\s+", "_");
	}

	private static final class RuneLiteSettings implements Settings
	{
		private final ConfigManager configManager;

		private RuneLiteSettings(ConfigManager configManager)
		{
			this.configManager = Objects.requireNonNull(configManager);
		}

		@Override
		public String get(String key)
		{
			return configManager.getConfiguration(CONFIG_GROUP, key);
		}

		@Override
		public void set(String key, String value)
		{
			configManager.setConfiguration(CONFIG_GROUP, key, value);
		}

		@Override
		public void unset(String key)
		{
			configManager.unsetConfiguration(CONFIG_GROUP, key);
		}
	}
}
