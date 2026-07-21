package com.nextmove.api;

import com.google.gson.Gson;
import com.nextmove.NextMoveVersion;
import com.nextmove.QuestSnapshot;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Timeout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NextMoveClientTest
{
	@Test
	public void browserReleaseUsesOneVersionEverywhere()
	{
		assertEquals("0.2.1", NextMoveVersion.CURRENT);
		assertEquals("Next-Move-RuneLite/0.2.1", NextMoveVersion.userAgent());
	}

	@Test
	public void postsTheCurrentCharactersQuestSnapshot()
	{
		FakeCallFactory factory = FakeCallFactory.respond(200, fixture("full-profile.json"));
		Result result = new Result();
		QuestSnapshot snapshot = new QuestSnapshot(
			"2026-07-20T15:00:00Z",
			"lastwilll",
			List.of(new QuestSnapshot.QuestEntry(
				"INTO_THE_TOMBS", "Into the Tombs", "NOT_STARTED")),
			"0.1.2").withCompletedRecommendationIds(List.of(
				"milestone:dragon-defender", "boss:vorkath"));

		new NextMoveClient(factory, new Gson()).load("lastwilll", snapshot, result);

		assertEquals(
			"https://osrsnextmove.com/api/runelite/profile?player=lastwilll",
			factory.capturedRequest.url().toString());
		assertEquals("POST", factory.capturedRequest.method());
		assertEquals("application/json; charset=utf-8",
			Objects.requireNonNull(factory.capturedRequest.body()).contentType().toString());
		assertTrue(requestBody(factory.capturedRequest).contains("\"INTO_THE_TOMBS\""));
		assertTrue(requestBody(factory.capturedRequest).contains(
			"\"completedRecommendationIds\":[\"milestone:dragon-defender\",\"boss:vorkath\"]"));
		assertNotNull(result.response);
		assertNull(result.failure);
	}

	@Test
	public void buildsOneFixedGetRequestAndParsesV1()
	{
		FakeCallFactory factory = FakeCallFactory.respond(200, fixture("full-profile.json"));
		Result result = load(factory, "italiaboi69");

		assertEquals(
			"https://osrsnextmove.com/api/runelite/profile?player=italiaboi69",
			factory.capturedRequest.url().toString());
		assertEquals("GET", factory.capturedRequest.method());
		assertEquals("application/json", factory.capturedRequest.header("Accept"));
		assertEquals(
			"Next-Move-RuneLite/0.2.1",
			factory.capturedRequest.header("User-Agent"));
		assertNotNull(result.response);
		assertNull(result.failure);
	}

	@Test
	public void rejectsRedirects()
	{
		FakeCallFactory factory = FakeCallFactory.respond(302, "redirect");
		assertEquals(ProfileFailure.Kind.UNAVAILABLE, load(factory, "italiaboi69").failure.getKind());
	}

	@Test
	public void mapsNotFoundAndRateLimitResponses()
	{
		assertEquals(
			ProfileFailure.Kind.NOT_FOUND,
			load(FakeCallFactory.respond(404, "missing"), "missing").failure.getKind());

		FakeCallFactory limited = FakeCallFactory.respond(429, "limited", "Retry-After", "37");
		ProfileFailure failure = load(limited, "italiaboi69").failure;
		assertEquals(ProfileFailure.Kind.RATE_LIMITED, failure.getKind());
		assertEquals(Integer.valueOf(37), failure.getRetryAfterSeconds());
	}

	@Test
	public void rejectsUnsupportedSchemasAndMalformedJson()
	{
		String unsupported = fixture("full-profile.json").replace(
			"\"schemaVersion\": 1", "\"schemaVersion\": 2");
		assertEquals(
			ProfileFailure.Kind.INCOMPATIBLE,
			load(FakeCallFactory.respond(200, unsupported), "italiaboi69").failure.getKind());
		assertEquals(
			ProfileFailure.Kind.MALFORMED,
			load(FakeCallFactory.respond(200, "{not-json"), "italiaboi69").failure.getKind());
	}

	@Test
	public void mapsNetworkFailuresWithoutReturningData()
	{
		FakeCallFactory factory = FakeCallFactory.fail(new IOException("offline"));
		Result result = load(factory, "italiaboi69");
		assertNull(result.response);
		assertEquals(ProfileFailure.Kind.UNAVAILABLE, result.failure.getKind());
	}

	@Test
	public void rejectsBodiesLargerThanTheDeclaredLimit()
	{
		String oversized = "x".repeat(262_145);
		assertEquals(
			ProfileFailure.Kind.MALFORMED,
			load(FakeCallFactory.respond(200, oversized), "italiaboi69").failure.getKind());
	}

	@Test
	public void rejectsInvalidUsernamesBeforeCreatingACall()
	{
		FakeCallFactory factory = FakeCallFactory.respond(200, fixture("full-profile.json"));
		Result result = load(factory, "https://bad.invalid");
		assertEquals(ProfileFailure.Kind.INVALID_USERNAME, result.failure.getKind());
		assertNull(factory.capturedRequest);
	}

	@Test
	public void exposesCancellationWithoutDeliveringAResult()
	{
		FakeCallFactory factory = FakeCallFactory.pending();
		Result result = new Result();
		ProfileRequest request = new NextMoveClient(factory, new Gson()).load(
			"italiaboi69", result);
		request.cancel();

		assertTrue(factory.call.isCanceled());
		assertNull(result.response);
		assertNull(result.failure);
	}

	private Result load(FakeCallFactory factory, String username)
	{
		Result result = new Result();
		new NextMoveClient(factory, new Gson()).load(username, result);
		return result;
	}

	private static String requestBody(Request request)
	{
		try
		{
			okio.Buffer buffer = new okio.Buffer();
			Objects.requireNonNull(request.body()).writeTo(buffer);
			return buffer.readUtf8();
		}
		catch (IOException exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static String fixture(String name)
	{
		try (InputStream stream = NextMoveClientTest.class.getResourceAsStream("/fixtures/" + name))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing fixture " + name);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException exception)
		{
			throw new IllegalStateException(exception);
		}
	}

	private static class Result implements NextMoveClient.Callback
	{
		private ProfileResponse response;
		private ProfileFailure failure;

		@Override
		public void onSuccess(ProfileResponse response)
		{
			this.response = response;
		}

		@Override
		public void onFailure(ProfileFailure failure)
		{
			this.failure = failure;
		}
	}

	private interface Behavior
	{
		void run(FakeCall call, Callback callback);
	}

	private static class FakeCallFactory implements Call.Factory
	{
		private final Behavior behavior;
		private Request capturedRequest;
		private FakeCall call;

		private FakeCallFactory(Behavior behavior)
		{
			this.behavior = behavior;
		}

		static FakeCallFactory respond(int code, String body, String... header)
		{
			return new FakeCallFactory((call, callback) -> {
				Response.Builder response = new Response.Builder()
					.request(call.request())
					.protocol(Protocol.HTTP_1_1)
					.code(code)
					.message("test")
					.body(ResponseBody.create(MediaType.get("application/json"), body));
				if (header.length == 2)
				{
					response.header(header[0], header[1]);
				}
				try
				{
					callback.onResponse(call, response.build());
				}
				catch (IOException exception)
				{
					throw new IllegalStateException(exception);
				}
			});
		}

		static FakeCallFactory fail(IOException failure)
		{
			return new FakeCallFactory((call, callback) -> callback.onFailure(call, failure));
		}

		static FakeCallFactory pending()
		{
			return new FakeCallFactory((call, callback) -> { });
		}

		@Override
		public Call newCall(Request request)
		{
			capturedRequest = request;
			call = new FakeCall(request, behavior);
			return call;
		}
	}

	private static class FakeCall implements Call
	{
		private final Request request;
		private final Behavior behavior;
		private boolean canceled;
		private boolean executed;

		private FakeCall(Request request, Behavior behavior)
		{
			this.request = request;
			this.behavior = behavior;
		}

		@Override
		public Request request()
		{
			return request;
		}

		@Override
		public Response execute()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void enqueue(Callback callback)
		{
			executed = true;
			behavior.run(this, callback);
		}

		@Override
		public void cancel()
		{
			canceled = true;
		}

		@Override
		public boolean isExecuted()
		{
			return executed;
		}

		@Override
		public boolean isCanceled()
		{
			return canceled;
		}

		@Override
		public Timeout timeout()
		{
			return Timeout.NONE;
		}

		@Override
		public Call clone()
		{
			return new FakeCall(request, behavior);
		}
	}
}
