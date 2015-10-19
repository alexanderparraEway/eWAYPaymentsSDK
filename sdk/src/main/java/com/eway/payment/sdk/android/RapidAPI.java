package com.eway.payment.sdk.android;

import com.eway.payment.sdk.android.beans.CodeDetail;
import com.eway.payment.sdk.android.beans.NVPair;
import com.eway.payment.sdk.android.beans.Status;
import com.eway.payment.sdk.android.beans.Transaction;
import com.eway.payment.sdk.android.entities.CodeLookupRequest;
import com.eway.payment.sdk.android.entities.CodeLookupResponse;
import com.eway.payment.sdk.android.entities.EncryptValuesRequest;
import com.eway.payment.sdk.android.entities.EncryptValuesResponse;
import com.eway.payment.sdk.android.entities.SubmitPaymentRequest;
import com.eway.payment.sdk.android.entities.SubmitPaymentResponse;
import com.eway.payment.sdk.android.entities.UserMessageResponse;
import com.eway.payment.sdk.android.entities.internal.InternalEncryptValuesResponse;
import com.eway.payment.sdk.android.entities.internal.InternalSubmitPaymentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Locale;

public class RapidAPI {
    private static final String PRODUCTION = "Production";
    private static final String PRODUCTION_URL = "https://api.ewaypayments.com/";
    private static final String SANDBOX = "Sandbox";
    private static final String SANDBOX_URL = "https://api.sandbox.ewaypayments.com/";
    private static final String PROCESSPAYMENT = "ProcessPayment";
    private static final String PAYMENT = "Payment";
    private static final String ECRYPT = "eCrypt";
    private static final String ENCRYPT = "encrypt";
    private static final String CODELOOKUP = "CodeLookup";
    private static final String VERSIONREPORTED = "1.1";
    public static String RapidEndpoint;
    public static String PublicAPIKey;

    private RapidAPI() {
    }

    public static SubmitPaymentResponse submitPayment(Transaction transaction) {
        if (transaction == null) {
            return new SubmitPaymentResponse("S9995", "", Status.Error);
        }
        try {
            SubmitPaymentRequest request = new SubmitPaymentRequest();
            request.setMethod(PROCESSPAYMENT);
            request.setCustomer(Utils.transformCustomer(transaction.getCustomer()));
            request.setShippingAddress(Utils.transformShippingAddress(transaction.getShippingDetails()));
            request.setShippingMethod(Utils.nullSafeGetShippingMethod(transaction));
            request.setItems(transaction.getLineItems());
            request.setOptions(Utils.transformOptions(transaction.getOptions()));
            request.setPayment(transaction.getPayment());
            request.setDeviceID(Utils.getUniquePsuedoID());
            request.setPartnerID(transaction.getPartnerID());
            request.setTransactionType(Utils.nullSafeGetTransactionType(transaction));
            InternalSubmitPaymentResponse internalResponse = Post(request, PAYMENT, InternalSubmitPaymentResponse.class);
            SubmitPaymentResponse response = new SubmitPaymentResponse();
            response.setErrors(internalResponse.getErrors());
            response.setStatus(parseStatus(internalResponse.getStatus()));
            response.setSubmissionID(internalResponse.getAccessCode());
            return response;
        } catch (RapidConfigurationException e) {
            return new SubmitPaymentResponse(e.getErrorCodes(), "", Status.Error);
        }
    }

    private static Status parseStatus(String status) {
        for (Status s : Status.values()) {
            if (s.toString().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }

    public static EncryptValuesResponse encryptValues(ArrayList<NVPair> Values) {
        try {
            EncryptValuesRequest request = new EncryptValuesRequest();
            EncryptValuesResponse response = new EncryptValuesResponse();
            request.setMethod(ECRYPT);
            request.setItems(Values);
            InternalEncryptValuesResponse internalResponse = Post(request, ENCRYPT, InternalEncryptValuesResponse.class);
            response.setErrors(internalResponse.getErrors());
            response.setStatus(Status.Success);
            response.setValues(internalResponse.getItems());
            return response;
        } catch (RapidConfigurationException e) {
            return new EncryptValuesResponse(e.getErrorCodes(), null, Status.Error);
        }
    }

    public static UserMessageResponse userMessage(String Language, String ErrorCodes) {
        try {
            CodeLookupResponse response = Post(new CodeLookupRequest(nullSafeGetLocale(Language), parseErrorCodeList(ErrorCodes)),
                    CODELOOKUP, CodeLookupResponse.class);
            ArrayList<String> errorMessages = new ArrayList<>();
            for (CodeDetail codeDetail : response.getCodeDetails()) {
                errorMessages.add(codeDetail.getDisplayMessage());
            }
            return new UserMessageResponse(errorMessages, null);
        } catch (RapidConfigurationException e) {
            return new UserMessageResponse(null, e.getErrorCodes());
        }
    }

    private static ArrayList<String> parseErrorCodeList(String ErrorCodes) {
        return (ErrorCodes != null) ? Utils.newArrayList(ErrorCodes.trim().replaceAll("\\s+", "").split(",")) : Utils.<String>newArrayList();
    }

    private static String nullSafeGetLocale(String Language) {
        return (Language != null) ? Language : Locale.getDefault().toString();
    }

    private static <V, R> R Post(V Request, String method, Class<? extends R> responseClass) throws RapidConfigurationException {
        try {
            DefaultHttpClient http = getEwayClient();
            Gson gson = new Gson();
            String baseUrl = RapidEndpoint;
            if (PRODUCTION.equalsIgnoreCase(baseUrl)) {
                baseUrl = PRODUCTION_URL;
            } else if (SANDBOX.equalsIgnoreCase(baseUrl)) {
                baseUrl = SANDBOX_URL;
            }
            HttpPost request = new HttpPost(buildUri(baseUrl, method));
            request.setHeader(HTTP.CONTENT_TYPE, "application/json;" + HTTP.UTF_8);
            StringEntity entity = new StringEntity(gson.toJson(Request), HTTP.UTF_8);
            request.setEntity(entity);
            HttpResponse response = http.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                return gson.fromJson(EntityUtils.toString(response.getEntity()), responseClass);
            } else if (response.getStatusLine().getStatusCode() == 401) {
                throw new RapidConfigurationException("S9993");
            } else if (response.getStatusLine().getStatusCode() == 443) {
                throw new RapidConfigurationException("S9991");
            } else {
                throw new RapidConfigurationException("S9992");
            }
        } catch (RapidConfigurationException ex) {
            throw ex;
        } catch (IllegalStateException ex) {
            throw new RapidConfigurationException("S9990");
        } catch (Exception ex) {
            throw new RapidConfigurationException("S9992");
        }

    }

    private static String buildUri(String baseUrl, String method) {
        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
        return baseUrl + method;
    }

    private static DefaultHttpClient getEwayClient() throws RapidConfigurationException {
        ArrayList<String> ErrorCodes = new ArrayList<>();
        if (PublicAPIKey == null || PublicAPIKey.isEmpty()) {
            ErrorCodes.add("S9991");
        }
        if (RapidEndpoint == null || RapidEndpoint.isEmpty()) {
            ErrorCodes.add("S9990");
        }
        if (ErrorCodes.size() > 0) {
            throw new RapidConfigurationException(ErrorCodes);
        }
        DefaultHttpClient http = new DefaultHttpClient();
        http.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                System.getProperty("http.agent") + ";eWAY SDK Android " + VERSIONREPORTED);
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(PublicAPIKey, ""));
        http.setCredentialsProvider(credProvider);
        return http;
    }
}
