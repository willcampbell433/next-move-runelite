package com.nextmove.completion;

import java.util.List;

final class CompletionLedger
{
	static final int VERSION = 1;

	private int version;
	private List<CompletedRecommendation> entries;

	@SuppressWarnings("unused")
	private CompletionLedger()
	{
	}

	CompletionLedger(List<CompletedRecommendation> entries)
	{
		this.version = VERSION;
		this.entries = List.copyOf(entries);
	}

	int getVersion()
	{
		return version;
	}

	List<CompletedRecommendation> getEntries()
	{
		return entries;
	}
}
