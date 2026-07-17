package com.nextmove.api;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProfileValidatorTest
{
	private final Gson gson = new Gson();

	@Test
	public void acceptsFullV1Profile()
	{
		ProfileResponse response = fixture("full-profile.json");
		ProfileValidator.validate(response);
		assertEquals(1, response.getSchemaVersion());
		assertEquals("italiaboi69", response.getProfile().getUsername());
	}

	@Test
	public void acceptsUnavailableQuestScore()
	{
		ProfileResponse response = fixture("partial-profile.json");
		ProfileValidator.validate(response);
		assertNull(response.getProfile().getAccount().getCategories().get(4).getScore());
	}

	@Test(expected = ProfileValidationException.class)
	public void rejectsUnsupportedSchema()
	{
		ProfileResponse response = fixture("full-profile.json");
		response.setSchemaVersionForTest(2);
		ProfileValidator.validate(response);
	}

	@Test(expected = ProfileValidationException.class)
	public void rejectsOutOfRangeAccountScore()
	{
		ProfileResponse response = fixture("full-profile.json");
		response.getProfile().getAccount().setScoreForTest(10001);
		ProfileValidator.validate(response);
	}

	@Test(expected = ProfileValidationException.class)
	public void rejectsUnknownCategoryIds()
	{
		ProfileResponse response = gson.fromJson(
			fixtureText("full-profile.json").replace("\"SKILLS\"", "\"NOT_A_CATEGORY\""),
			ProfileResponse.class);
		ProfileValidator.validate(response);
	}

	@Test(expected = ProfileValidationException.class)
	public void rejectsNullScoresMarkedAvailable()
	{
		ProfileResponse response = gson.fromJson(
			fixtureText("full-profile.json").replace(
				"\"score\": 92, \"maximumScore\"", "\"score\": null, \"maximumScore\""),
			ProfileResponse.class);
		ProfileValidator.validate(response);
	}

	@Test(expected = ProfileValidationException.class)
	public void rejectsUnparseableTimestamps()
	{
		ProfileResponse response = gson.fromJson(
			fixtureText("full-profile.json").replace(
				"2026-07-17T14:00:00.000Z", "not-a-timestamp"),
			ProfileResponse.class);
		ProfileValidator.validate(response);
	}

	private ProfileResponse fixture(String name)
	{
		return gson.fromJson(fixtureText(name), ProfileResponse.class);
	}

	private String fixtureText(String name)
	{
		InputStream stream = getClass().getResourceAsStream("/fixtures/" + name);
		if (stream == null)
		{
			throw new IllegalStateException("Missing fixture " + name);
		}
		try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
		{
			StringBuilder text = new StringBuilder();
			char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) >= 0)
			{
				text.append(buffer, 0, read);
			}
			return text.toString();
		}
		catch (Exception exception)
		{
			throw new IllegalStateException("Unable to load fixture " + name, exception);
		}
	}
}
