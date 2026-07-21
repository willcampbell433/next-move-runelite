package com.nextmove.completion;

import com.google.gson.Gson;
import com.nextmove.api.ProfileResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompletionStoreTest
{
	private final Gson gson = new Gson();
	private final FakeSettings settings = new FakeSettings();
	private final CompletionStore store = new CompletionStore(settings, gson);

	@Test
	public void isolatesLedgersByNormalizedAccountName()
	{
		store.markDone("Last Willl", recommendation("idea:one", "First", "SKILLING"),
			Instant.parse("2026-07-21T15:00:00Z"));

		assertEquals(List.of("idea:one"), store.completedIds("last_willl"));
		assertTrue(store.completedIds("different").isEmpty());
	}

	@Test
	public void roundTripsTheVersionedJsonPayload()
	{
		store.markDone("lastwilll", recommendation(
			"milestone:dragon-defender", "Earn the dragon defender", "UNLOCK"),
			Instant.parse("2026-07-21T15:00:00Z"));

		String json = settings.values.get("completedRecommendations.lastwilll");
		assertTrue(json.contains("\"version\":1"));
		assertTrue(json.contains("milestone:dragon-defender"));
		CompletedRecommendation restored = new CompletionStore(settings, gson)
			.load("LASTWILLL").get(0);
		assertEquals("Earn the dragon defender", restored.getTitle());
		assertEquals("UNLOCK", restored.getCategory());
		assertEquals("2026-07-21T15:00:00Z", restored.getCompletedAt());
	}

	@Test
	public void keepsTheNewestTwoHundredUniqueEntries()
	{
		for (int index = 0; index < 201; index++)
		{
			store.markDone("lastwilll", recommendation(
				"idea:" + index, "Idea " + index, "SKILLING"),
				Instant.parse("2026-07-21T15:00:00Z").plusSeconds(index));
		}

		List<CompletedRecommendation> entries = store.load("lastwilll");
		assertEquals(200, entries.size());
		assertEquals("idea:200", entries.get(0).getId());
		assertEquals("idea:1", entries.get(199).getId());
	}

	@Test
	public void replacesADuplicateAndRestoresIt()
	{
		store.markDone("lastwilll", recommendation("idea:one", "Old title", "PVM"),
			Instant.parse("2026-07-21T15:00:00Z"));
		store.markDone("lastwilll", recommendation("idea:one", "New title", "UNLOCK"),
			Instant.parse("2026-07-21T16:00:00Z"));

		assertEquals(1, store.load("lastwilll").size());
		assertEquals("New title", store.load("lastwilll").get(0).getTitle());
		store.restore("lastwilll", "idea:one");
		assertTrue(store.load("lastwilll").isEmpty());
	}

	@Test
	public void corruptOrInvalidJsonFallsBackToAnEmptyLedger()
	{
		settings.values.put("completedRecommendations.lastwilll", "{");
		assertTrue(store.load("lastwilll").isEmpty());

		settings.values.put("completedRecommendations.lastwilll",
			"{\"version\":2,\"entries\":[]}");
		assertTrue(store.load("lastwilll").isEmpty());

		settings.values.put("completedRecommendations.lastwilll",
			"{\"version\":1,\"entries\":[{\"id\":\"\",\"title\":\"Bad\",\"category\":\"UNLOCK\",\"completedAt\":\"not-a-date\"}]}");
		assertTrue(store.load("lastwilll").isEmpty());
	}

	private ProfileResponse.Recommendation recommendation(
		String id,
		String title,
		String category)
	{
		return gson.fromJson(String.format(
			"{\"id\":\"%s\",\"title\":\"%s\",\"category\":\"%s\",\"rationale\":\"Why\",\"evidence\":[],\"unlock\":\"Next\",\"nextMoveView\":\"coach\",\"wikiTitle\":\"Guide\"}",
			id,
			title,
			category), ProfileResponse.Recommendation.class);
	}

	private static final class FakeSettings implements CompletionStore.Settings
	{
		private final Map<String, String> values = new HashMap<>();

		@Override
		public String get(String key)
		{
			return values.get(key);
		}

		@Override
		public void set(String key, String value)
		{
			values.put(key, value);
		}

		@Override
		public void unset(String key)
		{
			values.remove(key);
		}
	}
}
