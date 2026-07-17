package com.nextmove.profile;

import com.nextmove.api.ProfileResponse;
import lombok.Getter;

@Getter
public final class ProfileState
{
	public enum Status
	{
		NOT_LOADED,
		LOADING,
		LOADED,
		LOADED_STALE,
		NOT_FOUND,
		RATE_LIMITED,
		UNAVAILABLE,
		INCOMPATIBLE_RESPONSE
	}

	private final Status status;
	private final String activeUsername;
	private final String currentCharacterUsername;
	private final boolean friendActive;
	private final ProfileResponse profile;
	private final String message;
	private final Integer retryAfterSeconds;

	private ProfileState(
		Status status,
		String activeUsername,
		String currentCharacterUsername,
		boolean friendActive,
		ProfileResponse profile,
		String message,
		Integer retryAfterSeconds)
	{
		this.status = status;
		this.activeUsername = activeUsername;
		this.currentCharacterUsername = currentCharacterUsername;
		this.friendActive = friendActive;
		this.profile = profile;
		this.message = message;
		this.retryAfterSeconds = retryAfterSeconds;
	}

	public static ProfileState notLoaded(String currentCharacterUsername)
	{
		return new ProfileState(
			Status.NOT_LOADED,
			currentCharacterUsername,
			currentCharacterUsername,
			false,
			null,
			"Public profile not loaded.",
			null);
	}

	static ProfileState loading(
		String username,
		String currentCharacterUsername,
		boolean friendActive)
	{
		return new ProfileState(
			Status.LOADING,
			username,
			currentCharacterUsername,
			friendActive,
			null,
			"Loading public profile…",
			null);
	}

	static ProfileState loaded(
		String username,
		String currentCharacterUsername,
		boolean friendActive,
		ProfileResponse profile)
	{
		return new ProfileState(
			Status.LOADED,
			username,
			currentCharacterUsername,
			friendActive,
			profile,
			"Public profile loaded.",
			null);
	}

	static ProfileState stale(
		String username,
		String currentCharacterUsername,
		boolean friendActive,
		ProfileResponse profile,
		String message,
		Integer retryAfterSeconds)
	{
		return new ProfileState(
			Status.LOADED_STALE,
			username,
			currentCharacterUsername,
			friendActive,
			profile,
			message,
			retryAfterSeconds);
	}

	static ProfileState failure(
		Status status,
		String username,
		String currentCharacterUsername,
		boolean friendActive,
		String message,
		Integer retryAfterSeconds)
	{
		return new ProfileState(
			status,
			username,
			currentCharacterUsername,
			friendActive,
			null,
			message,
			retryAfterSeconds);
	}

	ProfileState withCurrentCharacter(String username)
	{
		return new ProfileState(
			status,
			activeUsername,
			username,
			friendActive,
			profile,
			message,
			retryAfterSeconds);
	}
}
