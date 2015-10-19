package com.eway.payment.sdk.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eway.payment.sdk.android.RapidAPI;
import com.eway.payment.sdk.android.beans.NVPair;
import com.eway.payment.sdk.android.entities.EncryptValuesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EncryptActivity extends Activity {
    Button button;
    Button switchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encrypt);
        addListenerOnButton();
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.submit);
        switchButton = (Button) findViewById(R.id.nextPage);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new HttpAsyncTask().execute();
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent nextScreen = new Intent(EncryptActivity.this,
                        CodeLookupActivity.class);

                EncryptActivity.this.startActivity(nextScreen);
            }
        });

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            new AlertDialog.Builder(EncryptActivity.this)
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
            ArrayList<NVPair> values = new ArrayList<>();
            values.add(new NVPair("Card", ((EditText) findViewById(R.id.cardNo)).getText().toString()));
            values.add(new NVPair("CVN", ((EditText) findViewById(R.id.cvn)).getText().toString()));
            EncryptValuesResponse response = RapidAPI.encryptValues(values);
            if (response.getErrors() == null){
                StringBuilder message = new StringBuilder();
                for(NVPair pair: response.getValues()){
                    message.append(pair.getName()).append(": ").append(pair.getValue()).append("\n");
                }
                return message.toString();
            }
            else
                return errorHandler(RapidAPI.userMessage(Locale.getDefault().getLanguage(), response.getErrors()).getErrorMessages());
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
