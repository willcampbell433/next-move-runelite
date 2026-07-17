package com.nextmove.api;

import lombok.Getter;

@Getter
public class ProfileFailure
{
	public enum Kind
	{
		INVALID_USERNAME,
		NOT_FOUND,
		RATE_LIMITED,
		UNAVAILABLE,
		INCOMPATIBLE,
		MALFORMED
	}

	private final Kind kind;
	private final String message;
	private final Integer retryAfterSeconds;

	public ProfileFailure(Kind kind, String message, Integer retryAfterSeconds)
	{
		this.kind = kind;
		this.message = message;
		this.retryAfterSeconds = retryAfterSeconds;
	}
}
