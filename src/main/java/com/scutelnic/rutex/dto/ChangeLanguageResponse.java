package com.scutelnic.rutex.dto;

public class ChangeLanguageResponse {
	private String redirectUrl;

	public ChangeLanguageResponse() {
	}

	public ChangeLanguageResponse(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
}


