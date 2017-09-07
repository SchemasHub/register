package com.tomecode.schemashub.server.common;

public class GenericErrorDto {

	private String message;
	private long date;

	public static GenericErrorDto NotFoundDocumentById(String id) {
		return new GenericErrorDto("Not found document by id: " + id);
	}

	public GenericErrorDto() {

	}

	public GenericErrorDto(String message) {
		this(message, System.currentTimeMillis());
	}

	public GenericErrorDto(String message, long date) {

		this.message = message;
		this.date = date;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public final long getDate() {
		return date;
	}

	public final void setDate(long date) {
		this.date = date;
	}

}
