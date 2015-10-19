package com.eway.payment.sdk.android.entities.internal;

import com.eway.payment.sdk.android.entities.EncryptValuesRequest;

public class InternalEncryptValuesResponse extends EncryptValuesRequest {
    private String Errors;

    public String getErrors() {
        return Errors;
    }

    public void setErrors(String errors) {
        Errors = errors;
    }
}
