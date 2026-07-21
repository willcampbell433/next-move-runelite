package com.nextmove.completion;

import com.nextmove.api.ProfileResponse;
import java.time.Instant;
import java.util.List;

public interface CompletionRepository
{
	List<CompletedRecommendation> load(String username);

	List<String> completedIds(String username);

	void markDone(
		String username,
		ProfileResponse.Recommendation recommendation,
		Instant completedAt);

	void restore(String username, String recommendationId);

	static CompletionRepository none()
	{
		return new CompletionRepository()
		{
			@Override
			public List<CompletedRecommendation> load(String username)
			{
				return List.of();
			}

			@Override
			public List<String> completedIds(String username)
			{
				return List.of();
			}

			@Override
			public void markDone(
				String username,
				ProfileResponse.Recommendation recommendation,
				Instant completedAt)
			{
			}

			@Override
			public void restore(String username, String recommendationId)
			{
			}
		};
	}
}
