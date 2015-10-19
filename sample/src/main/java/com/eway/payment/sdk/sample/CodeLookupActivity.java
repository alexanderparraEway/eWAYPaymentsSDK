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
import com.eway.payment.sdk.android.entities.UserMessageResponse;

import java.util.List;
import java.util.Locale;

public class CodeLookupActivity extends Activity {
    Button button;
    Button switchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codelookup);
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
                Intent nextScreen = new Intent(CodeLookupActivity.this,
                        EwaySampleActivity.class);

                CodeLookupActivity.this.startActivity(nextScreen);
            }
        });

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            new AlertDialog.Builder(CodeLookupActivity.this)
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
            String errorCodes = ((EditText) findViewById(R.id.errorCodes)).getText().toString();
            UserMessageResponse response = RapidAPI.userMessage(Locale.getDefault().getLanguage(), errorCodes);
            if (!response.getErrorMessages().isEmpty())
                return resultHandler(response.getErrorMessages());
            else
                return response.getErrors();
        }

        private String resultHandler(List<String> response) {
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