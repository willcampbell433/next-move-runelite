package com.nextmove.api;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nextmove.NextMoveVersion;
import com.nextmove.QuestSnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NextMoveClient
{
	private static final long MAX_RESPONSE_BYTES = 262_144L;
	private static final HttpUrl ENDPOINT = Objects.requireNonNull(
		HttpUrl.parse("https://osrsnextmove.com/api/runelite/profile"));
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9 _-]{1,12}");
	private static final String USER_AGENT = "Next-Move-RuneLite/" + NextMoveVersion.CURRENT;
	private static final okhttp3.MediaType JSON = okhttp3.MediaType.get(
		"application/json; charset=utf-8");

	private final Call.Factory callFactory;
	private final Gson gson;

	@Inject
	public NextMoveClient(OkHttpClient httpClient, Gson gson)
	{
		this(
			(Call.Factory) httpClient.newBuilder()
				.followRedirects(false)
				.followSslRedirects(false)
				.callTimeout(10, TimeUnit.SECONDS)
				.build(),
			gson);
	}

	NextMoveClient(Call.Factory callFactory, Gson gson)
	{
		this.callFactory = Objects.requireNonNull(callFactory);
		this.gson = Objects.requireNonNull(gson);
	}

	public interface Callback
	{
		void onSuccess(ProfileResponse response);

		void onFailure(ProfileFailure failure);
	}

	public ProfileRequest load(String username, Callback callback)
	{
		return load(username, null, callback);
	}

	public ProfileRequest load(
		String username,
		QuestSnapshot snapshot,
		Callback callback)
	{
		Objects.requireNonNull(callback);
		String validated = username == null ? "" : username.trim();
		if (!USERNAME.matcher(validated).matches())
		{
			callback.onFailure(failure(
				ProfileFailure.Kind.INVALID_USERNAME,
				"Enter a valid 1–12 character OSRS username."));
			return () -> { };
		}

		if (snapshot != null
			&& !validated.equalsIgnoreCase(snapshot.getAccount().getDisplayName()))
		{
			callback.onFailure(failure(
				ProfileFailure.Kind.INVALID_USERNAME,
				"Quest snapshot does not match the selected character."));
			return () -> { };
		}

		Request.Builder requestBuilder = new Request.Builder()
			.url(ENDPOINT.newBuilder().addQueryParameter("player", validated).build())
			.header("Accept", "application/json")
			.header("User-Agent", USER_AGENT);
		Request request = snapshot == null
			? requestBuilder.get().build()
			: requestBuilder.post(RequestBody.create(JSON, gson.toJson(snapshot))).build();
		Call call = callFactory.newCall(request);
		call.enqueue(new okhttp3.Callback()
		{
			@Override
			public void onFailure(Call failedCall, IOException exception)
			{
				if (!failedCall.isCanceled())
				{
					callback.onFailure(failure(
						ProfileFailure.Kind.UNAVAILABLE,
						"Next Move is temporarily unavailable."));
				}
			}

			@Override
			public void onResponse(Call completedCall, Response response)
			{
				if (completedCall.isCanceled())
				{
					response.close();
					return;
				}
				handleResponse(response, callback);
			}
		});
		return call::cancel;
	}

	private void handleResponse(Response response, Callback callback)
	{
		try (Response closed = response)
		{
			int code = closed.code();
			if (code == 404)
			{
				callback.onFailure(failure(
					ProfileFailure.Kind.NOT_FOUND,
					"That player was not found on the public Hiscores."));
				return;
			}
			if (code == 429)
			{
				callback.onFailure(new ProfileFailure(
					ProfileFailure.Kind.RATE_LIMITED,
					"Too many public lookups. Try again shortly.",
					parseRetryAfter(closed.header("Retry-After"))));
				return;
			}
			if (code >= 300 && code < 400)
			{
				callback.onFailure(failure(
					ProfileFailure.Kind.UNAVAILABLE,
					"Next Move refused an unexpected redirect."));
				return;
			}
			if (!closed.isSuccessful())
			{
				callback.onFailure(failure(
					ProfileFailure.Kind.UNAVAILABLE,
					"Next Move is temporarily unavailable."));
				return;
			}

			ResponseBody body = closed.body();
			if (body == null)
			{
				callback.onFailure(malformed());
				return;
			}
			long contentLength = body.contentLength();
			if (contentLength > MAX_RESPONSE_BYTES)
			{
				callback.onFailure(malformed());
				return;
			}
			byte[] bytes = readBounded(body.byteStream());
			if (bytes == null)
			{
				callback.onFailure(malformed());
				return;
			}

			ProfileResponse profile;
			try
			{
				profile = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), ProfileResponse.class);
				ProfileValidator.validate(profile);
			}
			catch (ProfileValidationException exception)
			{
				ProfileFailure.Kind kind = exception.getMessage() != null
					&& exception.getMessage().startsWith("Unsupported schema")
					? ProfileFailure.Kind.INCOMPATIBLE
					: ProfileFailure.Kind.MALFORMED;
				callback.onFailure(failure(
					kind,
					kind == ProfileFailure.Kind.INCOMPATIBLE
						? "Next Move needs a plugin update."
						: "Next Move returned an invalid response."));
				return;
			}
			catch (JsonParseException | IllegalStateException exception)
			{
				callback.onFailure(malformed());
				return;
			}

			callback.onSuccess(profile);
		}
		catch (IOException exception)
		{
			callback.onFailure(failure(
				ProfileFailure.Kind.UNAVAILABLE,
				"Next Move is temporarily unavailable."));
		}
	}

	private static byte[] readBounded(InputStream stream) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8_192];
		long remaining = MAX_RESPONSE_BYTES + 1;
		while (remaining > 0)
		{
			int read = stream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
			if (read < 0)
			{
				break;
			}
			output.write(buffer, 0, read);
			remaining -= read;
		}
		return output.size() > MAX_RESPONSE_BYTES ? null : output.toByteArray();
	}

	private static Integer parseRetryAfter(String value)
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			int seconds = Integer.parseInt(value);
			return seconds > 0 ? seconds : null;
		}
		catch (NumberFormatException exception)
		{
			return null;
		}
	}

	private static ProfileFailure malformed()
	{
		return failure(
			ProfileFailure.Kind.MALFORMED,
			"Next Move returned an invalid response.");
	}

	private static ProfileFailure failure(ProfileFailure.Kind kind, String message)
	{
		return new ProfileFailure(kind, message, null);
	}
}
