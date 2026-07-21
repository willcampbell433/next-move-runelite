package com.nextmove;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

final class QuestSnapshotBuilder
{
	private final String pluginVersion;
	private final Clock clock;

	QuestSnapshotBuilder(String pluginVersion)
	{
		this(pluginVersion, Clock.systemUTC());
	}

	QuestSnapshotBuilder(String pluginVersion, Clock clock)
	{
		this.pluginVersion = Objects.requireNonNull(pluginVersion);
		this.clock = Objects.requireNonNull(clock);
	}

	QuestSnapshot build(Client client, String displayName)
	{
		Objects.requireNonNull(client);
		return build(displayName, quest -> quest.getState(client));
	}

	QuestSnapshot build(
		String displayName,
		Function<Quest, QuestState> stateReader)
	{
		Objects.requireNonNull(stateReader);
		return new QuestSnapshot(
			Instant.now(clock).toString(),
			displayName,
			Arrays.stream(Quest.values())
				.map(quest -> new QuestSnapshot.QuestEntry(
					quest.name(),
					quest.getName(),
					Objects.requireNonNull(stateReader.apply(quest)).name()))
				.collect(Collectors.toList()),
			pluginVersion);
	}
}
