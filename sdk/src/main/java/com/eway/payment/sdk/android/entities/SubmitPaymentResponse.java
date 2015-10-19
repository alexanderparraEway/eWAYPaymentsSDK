package com.eway.payment.sdk.android.entities;

import com.eway.payment.sdk.android.beans.Status;

public class SubmitPaymentResponse {
    private String Errors;
    private String SubmissionID;
    private com.eway.payment.sdk.android.beans.Status Status;

    public SubmitPaymentResponse() {
    }

    public SubmitPaymentResponse(String errors, String submissionID, com.eway.payment.sdk.android.beans.Status status) {
        Errors = errors;
        SubmissionID = submissionID;
        Status = status;
    }

    public Status getStatus() {
        return Status;
    }

    public void setStatus(Status status) {
        Status = status;
    }

    public String getErrors() {
        return Errors;
    }

    public void setErrors(String errors) {
        Errors = errors;
    }

    public String getSubmissionID() {
        return SubmissionID;
    }

    public void setSubmissionID(String submissionID) {
        SubmissionID = submissionID;
    }
}
