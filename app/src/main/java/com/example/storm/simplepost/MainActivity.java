package com.example.storm.simplepost;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText  phoneEdit, iCodeEdit;
    TextView postButton;

    public static MainActivity sharedInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedInstance = this;
        InitView();
    }

    private void InitView(){
        phoneEdit = getPhoneEdit();
        iCodeEdit = getiCodeEdit();

        postButton = getPostButton();

    }



    private EditText getPhoneEdit(){
        if (phoneEdit == null){
            phoneEdit = (EditText)findViewById(R.id.phone);
        }
        return phoneEdit;
    }

    private EditText getiCodeEdit(){
        if (iCodeEdit == null){
            iCodeEdit = (EditText)findViewById(R.id.icode);
        }
        return iCodeEdit;
    }

    private TextView getPostButton(){
        if (postButton == null){
            postButton = (TextView)findViewById(R.id.postButton);
            postButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doPost();
                }
            });
        }
        return postButton;
    }

    private boolean checkEmptyFields(){

        String phoneStr = getPhoneEdit().getText().toString();
        if (phoneStr.length() == 0){
            Utility.showAlert(this, "Alert", "Phone number can not be empty!");
            getPhoneEdit().requestFocus();
            return false;
        }

        if (phoneStr.length() > 11){
            Utility.showAlert(this, "Alert", "Phone number can not be shorter than 11!");
            getPhoneEdit().requestFocus();
            return false;
        }


        if (getiCodeEdit().getText().toString().length() == 0){
            Utility.showAlert(this, "Alert", "Internal Code can not be empty!");
            getPhoneEdit().requestFocus();
            return false;
        }

        if (getiCodeEdit().getText().toString().length() != 3){
            Utility.showAlert(this, "Alert", "Internal Code must be 3 digit!");
            getPhoneEdit().requestFocus();
            return false;
        }


        return true;
    }

    private void doPost(){

        if (!checkEmptyFields())
            return;

        String phone = getPhoneEdit().getText().toString().trim();
        String iCode = getiCodeEdit().getText().toString().trim();

        String urlParameters = "";
        try{
             urlParameters =
                    "phone=" + URLEncoder.encode(phone, "UTF-8") +
                            "&internationalCode=" + URLEncoder.encode(iCode, "UTF-8");

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        new postTask().execute(Constant.postUrl, urlParameters);
    }

    private  class  postTask extends AsyncTask<String, Void, List<String>> {

        protected void onPreExecute() {
            // TODO Auto-generated method stub
            Utility.show_LoadingIndicator("Posting data ....", MainActivity.sharedInstance);
        }

        @Override
        protected List<String> doInBackground(String... params) {
            // TODO Auto-generated method stub
            List<String> result = new ArrayList<>();
            String url = params[0];
            String param = params[1];
            String response = excutePost(url, param);
            if (response != null)
                result.add(response);

            return result;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            Utility.hideLoading();

            if (result != null){
                if (result.size() >0)
                    Log.d("response", result.get(0).toString());
            }
        }
    }

    public static String excutePost(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type",
//                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Type",
                    "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json, multipart/related, text/*");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (connection instanceof HttpsURLConnection){
                ConnectionUtils.resetConnection(connection);
            }

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int status = connection.getResponseCode();
            System.out.println(status);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
