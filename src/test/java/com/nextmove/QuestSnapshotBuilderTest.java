package com.nextmove;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuestSnapshotBuilderTest
{
	private static final int SUPPORTED_QUEST_COUNT = 210;

	@Test
	public void capturesEveryRuneLiteQuestAsOneCompleteSnapshot()
	{
		Clock clock = Clock.fixed(
			Instant.parse("2026-07-20T15:00:00Z"),
			ZoneOffset.UTC);
		QuestSnapshotBuilder builder = new QuestSnapshotBuilder("0.1.2", clock);

		QuestSnapshot snapshot = builder.build(
			"lastwilll",
			quest -> quest == Quest.INTO_THE_TOMBS
				? QuestState.IN_PROGRESS
				: QuestState.FINISHED);

		assertEquals(1, snapshot.getSchemaVersion());
		assertEquals("2026-07-20T15:00:00Z", snapshot.getCapturedAt());
		assertEquals("lastwilll", snapshot.getAccount().getDisplayName());
		assertEquals("0.1.2", snapshot.getSource().getPluginVersion());
		assertEquals(SUPPORTED_QUEST_COUNT, Quest.values().length);
		assertEquals(Quest.values().length, snapshot.getQuests().size());
		assertTrue(snapshot.getQuests().stream().anyMatch(quest ->
			"INTO_THE_TOMBS".equals(quest.getKey())
				&& "Into the Tombs".equals(quest.getName())
				&& "IN_PROGRESS".equals(quest.getState())));
	}
}
