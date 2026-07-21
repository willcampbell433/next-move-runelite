package com.nextmove.completion;

import java.util.Objects;

public final class CompletedRecommendation
{
	private final String id;
	private final String title;
	private final String category;
	private final String completedAt;

	public CompletedRecommendation(
		String id,
		String title,
		String category,
		String completedAt)
	{
		this.id = Objects.requireNonNull(id);
		this.title = Objects.requireNonNull(title);
		this.category = Objects.requireNonNull(category);
		this.completedAt = Objects.requireNonNull(completedAt);
	}

	public String getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getCategory()
	{
		return category;
	}

	public String getCompletedAt()
	{
		return completedAt;
	}
}
