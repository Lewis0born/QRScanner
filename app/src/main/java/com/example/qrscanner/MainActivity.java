package com.example.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import android.widget.Button;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import com.google.zxing.integration.android.IntentIntegrator;


public class MainActivity extends AppCompatActivity {

    Button scanBtn;
    String scannedData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity activity = this;
        scanBtn = findViewById(R.id.scan_btn);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("");
                integrator.setBeepEnabled(true);
                integrator.setCameraId(0);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null) {
            scannedData = result.getContents();
            if (scannedData != null) {
                // Here we need to handle scanned data...
                // send data to sheet (move to showAlertDialog)
                //new SendRequest().execute();
                showAlertDialog();
            }else {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAlertDialog() {
        // show popup when barcode is scanned
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Barcode Information:");
        alertDialogBuilder.setMessage(scannedData);

        // 4 Buttons: IN, OUT, SOLD, and CANCEL
        alertDialogBuilder.setPositiveButton("Coming In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog("Confirm Coming In?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, send data to IN
                        new SendRequest("IN").execute();
                        showToast("Coming In request sent!");
                    }
                });
            }
        });

        alertDialogBuilder.setNeutralButton("Delivered", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog("Confirm Delivery?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, send data to OUT
                        new SendRequest("OUT").execute();
                        showToast("Delivery request sent!");
                    }
                });
            }
        });

        alertDialogBuilder.setNegativeButton("Sold", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog("Confirm Sold?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, send data to SOLD
                        new SendRequest("SOLD").execute();
                        showToast("Sold request sent!");
                    }
                });
            }
        });

        // Show popup
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showConfirmationDialog(String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(this);
        confirmationDialogBuilder.setMessage(message);
        confirmationDialogBuilder.setPositiveButton(positiveText, positiveClickListener);
        confirmationDialogBuilder.setNegativeButton(negativeText, null);
        AlertDialog confirmationDialog = confirmationDialogBuilder.create();
        confirmationDialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }





    public class SendRequest extends AsyncTask<String, Void, String> {

        // Determines whether we're sending data to "IN" or "OUT" column
        private String action;
        // Constructor to set the action
        public SendRequest(String action) {
            this.action = action;
        }


        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                // google script URL Here
                URL url = new URL("https://script.google.com/macros/s/AKfycbytCW7XJdn5uJ5Os3n4IinwTcMdTyt4M5R1wWh6Ppc1Abj7wMhEJXzpmrUzyMHSooqjWg/exec");
                JSONObject postDataParams = new JSONObject();

                //Passing scanned code and action as parameters
                postDataParams.put("sdata",scannedData);
                postDataParams.put("action", action);

                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    //error here: false:401 (fixed, google scripts permissions issue)
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}