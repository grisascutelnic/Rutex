package com.scutelnic.rutex.dto;

public class LoginResponseDTO {
	private boolean success;
	private String message;
	private String userEmail;
	private Long userId;
	private Integer sessionTimeout;
	private Boolean rememberMe;

	public LoginResponseDTO() {}

	public LoginResponseDTO(boolean success, String message, String userEmail, Long userId, Integer sessionTimeout, Boolean rememberMe) {
		this.success = success;
		this.message = message;
		this.userEmail = userEmail;
		this.userId = userId;
		this.sessionTimeout = sessionTimeout;
		this.rememberMe = rememberMe;
	}

	public boolean isSuccess() { return success; }
	public void setSuccess(boolean success) { this.success = success; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public String getUserEmail() { return userEmail; }
	public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public Integer getSessionTimeout() { return sessionTimeout; }
	public void setSessionTimeout(Integer sessionTimeout) { this.sessionTimeout = sessionTimeout; }

	public Boolean getRememberMe() { return rememberMe; }
	public void setRememberMe(Boolean rememberMe) { this.rememberMe = rememberMe; }
}


