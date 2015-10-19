package com.eway.payment.sdk.android;

import java.util.ArrayList;

class RapidConfigurationException extends Exception {
    private ArrayList<String> ErrorCodes;

    public RapidConfigurationException(String... errorCodes) {
        ErrorCodes = Utils.newArrayList(errorCodes);
    }

    public RapidConfigurationException(ArrayList<String> errorCodes) {
        ErrorCodes = errorCodes;
    }

    public String getErrorCodes() {
        return Utils.join(",", ErrorCodes);
    }
}
