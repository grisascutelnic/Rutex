package com.scutelnic.rutex.dto;

public class RegisterResponseDTO {
	private boolean success;
	private String message;
	private String userEmail;
	private Long userId;

	public RegisterResponseDTO() {}

	public RegisterResponseDTO(boolean success, String message, String userEmail, Long userId) {
		this.success = success;
		this.message = message;
		this.userEmail = userEmail;
		this.userId = userId;
	}

	public boolean isSuccess() { return success; }
	public void setSuccess(boolean success) { this.success = success; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public String getUserEmail() { return userEmail; }
	public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
}


