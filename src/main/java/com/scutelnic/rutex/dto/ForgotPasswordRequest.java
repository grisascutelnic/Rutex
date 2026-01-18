package com.scutelnic.rutex.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
    private String recaptchaResponse;
}
