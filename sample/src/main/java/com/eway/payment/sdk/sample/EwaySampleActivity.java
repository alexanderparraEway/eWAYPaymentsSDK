package com.eway.payment.sdk.sample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eway.payment.sdk.android.RapidAPI;
import com.eway.payment.sdk.android.beans.CardDetails;
import com.eway.payment.sdk.android.beans.Customer;
import com.eway.payment.sdk.android.beans.NVPair;
import com.eway.payment.sdk.android.beans.Payment;
import com.eway.payment.sdk.android.beans.Transaction;
import com.eway.payment.sdk.android.beans.TransactionType;
import com.eway.payment.sdk.android.entities.EncryptValuesResponse;
import com.eway.payment.sdk.android.entities.SubmitPaymentResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EwaySampleActivity extends Activity {
    Button button;
    Button switchButton;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addListenerOnButton();
        transTypesPopulator();
        dateTimePopolator();
        RapidAPI.PublicAPIKey = "epk-AC103B1C-9866-4D52-BB60-F4296063AD21";
        RapidAPI.RapidEndpoint = "https://api.sandbox.ewaypayments.com/staging-au/";

    }

    private void dateTimePopolator() {
        Spinner monthSpinner = (Spinner) findViewById(R.id.expMonth);
        Spinner yearSpinner = (Spinner) findViewById(R.id.expYear);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, makeSequence(1, 12));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(dataAdapter);

        dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, makeSequence
                (Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.YEAR) + 10));
        yearSpinner.setAdapter(dataAdapter);
    }

    private List<String> makeSequence(int begin, int end) {
        List<String> ret = new ArrayList(end - begin + 1);

        for (int i = begin; i <= end; ret.add(
                ((end > 12 ? "0000" : "00") + Integer.toString(i)).substring(Integer.toString(i++).length())))
            ;

        return ret;
    }

    private void transTypesPopulator() {
        Spinner transTypesSpinner = (Spinner) findViewById(R.id.transTypes);
        List<String> list = new ArrayList<>();
        for (TransactionType type : TransactionType.values()) {
            list.add(type.name());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transTypesSpinner.setAdapter(dataAdapter);
    }


    private void addListenerOnButton() {

        button = (Button) findViewById(R.id.submit);
        switchButton = (Button) findViewById(R.id.nextPage);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                progressDialog = ProgressDialog.show(EwaySampleActivity.this, "Processing", "Processing payment", true);
                new HttpAsyncTask().execute();
            }
        });

        switchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent nextScreen = new Intent(EwaySampleActivity.this,
                        EncryptActivity.class);
                EwaySampleActivity.this.startActivity(nextScreen);
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            new AlertDialog.Builder(EwaySampleActivity.this)
                    .setTitle("Result")
                    .setMessage(s)
                    .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create()
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {
            Transaction transaction = new Transaction();
            Payment payment = new Payment();
            CardDetails cardDetails = new CardDetails();
            Customer customer = new Customer();
            try {
                payment.setTotalAmount(Integer.parseInt(((EditText) findViewById(R.id.totalAmount)).getText().toString().isEmpty() ?
                        "0" : ((EditText) findViewById(R.id.totalAmount)).getText().toString()));
            } catch (NumberFormatException ex) {
                payment.setTotalAmount(0);
            }
            cardDetails.setName(((EditText) findViewById(R.id.cardName)).getText().toString());
            //Encrypt card data before sending
            EncryptValuesResponse nCryptedData = encryptCard(((EditText) findViewById(R.id.cardNumber)).getText().toString(),
                    ((EditText) findViewById(R.id.cvn)).getText().toString());
            if (nCryptedData.getErrors() != null)
                return errorHandler(RapidAPI.userMessage(Locale.getDefault().getLanguage(), nCryptedData.getErrors()).getErrorMessages());
            cardDetails.setNumber(nCryptedData.getValues().get(0).getValue());
            cardDetails.setCVN(nCryptedData.getValues().get(1).getValue());
            cardDetails.setExpiryMonth(((Spinner) findViewById(R.id.expMonth)).getSelectedItem().toString());
            cardDetails.setExpiryYear(((Spinner) findViewById(R.id.expYear)).getSelectedItem().toString());
            customer.setCardDetails(cardDetails);
            transaction.setTransactionType(TransactionType.valueOf(((Spinner) findViewById(R.id.transTypes)).getSelectedItem().toString().isEmpty() ?
                    TransactionType.Purchase.toString() : ((Spinner) findViewById(R.id.transTypes)).getSelectedItem().toString()));
            transaction.setPayment(payment);
            transaction.setCustomer(customer);
            SubmitPaymentResponse response = RapidAPI.submitPayment(transaction);
            if (response.getErrors() == null)
                return ("Succeed. Submission ID is: " + response.getSubmissionID());
            else {
                return errorHandler(RapidAPI.userMessage(Locale.getDefault().getLanguage(), response.getErrors()).getErrorMessages());
            }
        }

        private EncryptValuesResponse encryptCard(String cardNo, String CVN) {
            ArrayList<NVPair> values = new ArrayList<>();
            values.add(new NVPair("Card", cardNo));
            values.add(new NVPair("CVN", CVN));
            return RapidAPI.encryptValues(values);
        }

        private String errorHandler(List<String> response) {
            StringBuilder result = new StringBuilder();
            int i = 0;
            for (String errorMsg : response) {
                result.append("Message ").append(i).append(" = ").append(errorMsg).append("\n");
                i++;
            }
            return result.toString();
        }
    }
}
