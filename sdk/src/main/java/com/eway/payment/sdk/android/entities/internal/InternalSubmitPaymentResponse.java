package com.eway.payment.sdk.android.entities.internal;

public class InternalSubmitPaymentResponse {
    private String AccessCode;
    private String Status;
    private String Errors;

    public String getAccessCode() {
        return AccessCode;
    }

    public void setAccessCode(String accessCode) {
        AccessCode = accessCode;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getErrors() {
        return Errors;
    }

    public void setErrors(String errors) {
        Errors = errors;
    }
}
