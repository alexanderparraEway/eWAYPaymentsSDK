package com.eway.payment.sdk.android.entities;

import com.eway.payment.sdk.android.beans.NVPair;

import java.util.ArrayList;

public class EncryptValuesResponse {
    private String Errors;
    private ArrayList<NVPair> Values;
    private com.eway.payment.sdk.android.beans.Status Status;

    public EncryptValuesResponse() {
    }

    public EncryptValuesResponse(String errors, ArrayList<NVPair> values, com.eway.payment.sdk.android.beans.Status status) {
        Errors = errors;
        Values = values;
        Status = status;
    }

    public com.eway.payment.sdk.android.beans.Status getStatus() {
        return Status;
    }

    public void setStatus(com.eway.payment.sdk.android.beans.Status status) {
        Status = status;
    }

    public String getErrors() {
        return Errors;
    }

    public void setErrors(String errors) {
        Errors = errors;
    }

    public ArrayList<NVPair> getValues() {
        return Values;
    }

    public void setValues(ArrayList<NVPair> values) {
        Values = values;
    }
}
