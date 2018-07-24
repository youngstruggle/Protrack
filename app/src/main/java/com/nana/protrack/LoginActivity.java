package com.nana.protrack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private EditText inputUsername;
    private EditText inputPassword;
    private EditText urlAddress;
    CheckBox checkRemember;
    ProgressBar progressBar;
    TextView txtResult;
    static Boolean errored = false;
    String username;
    String password;
    String url;
    protected Cursor cursor, cursorRemember;
    DataHelper dbHelper;
    String rememberMe = "N";
    String wsResponse = "Invalid Response";
    private ProgressDialog pDialog;
    DataHelper dbcenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
//        urlAddress = (EditText) findViewById(R.id.url_address);
        checkRemember = (CheckBox) findViewById(R.id.checkRemember);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtResult = (TextView) findViewById(R.id.txtResult);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        dbHelper = new DataHelper(this);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        dbcenter = new DataHelper(this);
        SQLiteDatabase db = dbcenter.getReadableDatabase();
        cursorRemember = db.rawQuery("select URL_ADDRESS, USERNAME from LOGINDATA WHERE FL_REMEMBER='Y'", null);
        cursorRemember.moveToFirst();
        System.out.println("Estim remember me count "+cursorRemember.getCount());
        if (cursorRemember.getCount() > 0){
            //urlAddress.setText(cursorRemember.getString(0).toString());
            inputUsername.setText(cursorRemember.getString(1).toString());
            checkRemember.setChecked(true);
        }

        checkRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    rememberMe = "Y";
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRemember.isChecked()){
                    rememberMe="Y";
                }
                else {
                    rememberMe="N";
                }
                username = inputUsername.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                url = "https://estim.co.id:8085/protrack";
                // Check for empty data in the form
                if (!username.isEmpty() && !password.isEmpty() && !url.isEmpty() ) {
                    // login user
                    txtResult.setText("");
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                } else {
                    // Prompt user to enter credentials
                    txtResult.setText("Please enter the credentials!");
                    Toast.makeText(getApplicationContext(),"Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Call Web Method
            wsResponse = WebServices.invokeLoginWS(username,password,url,"loginauthentication");
            return null;
        }

        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {
            //Make Progress Bar invisible
//            progressBar.setVisibility(View.INVISIBLE);
            hideDialog();
            Intent intObj = new Intent(LoginActivity.this,WebViewActivity.class);
            //Error status is false
            if(!errored){
                //Based on Boolean value returned from WebService
                if(wsResponse != null && wsResponse.equalsIgnoreCase("1")){
                    if(!rememberMe.equalsIgnoreCase("Y")){
                        String urlEp = url+"/page/protrack/index.zul?user="+username+"&password="+password;
                        Bundle bundle = new Bundle();
                        //Add your data to bundle
                        bundle.putString("remember", "false");
                        bundle.putString("url", urlEp);
                        bundle.putString("wsresponse", wsResponse);
                        //Add the bundle to the intent
                        intObj.putExtras(bundle);
                        if(isDataExist()){
                            deleteData();
                        }
                    }else{
                        Bundle bundle = new Bundle();
                        //Add your data to bundle
                        bundle.putString("remember", "true");
                        bundle.putString("url", "empty");
                        bundle.putString("wsresponse", wsResponse);
                        //Add the bundle to the intent
                        intObj.putExtras(bundle);
                        if(isDataExist()){
                            deleteData();
                        }
                        insertData(username, password, url, rememberMe);
                    }
                    //Navigate to WebView Screen
                    startActivity(intObj);
                    finish();
                }else if(wsResponse != null && wsResponse.equalsIgnoreCase("2")) {
                    if(!rememberMe.equalsIgnoreCase("Y")){
                        String urlAdm = url+"/page/protrack/index.zul?user="+username+"&password="+password;
                        Bundle bundle = new Bundle();
                        //Add your data to bundle
                        bundle.putString("remember", "false");
                        bundle.putString("url", urlAdm);
                        bundle.putString("wsresponse", wsResponse);
                        //Add the bundle to the intent
                        intObj.putExtras(bundle);
                        if(isDataExist()){
                            deleteData();
                        }
                    }else{
                        Bundle bundle = new Bundle();
                        //Add your data to bundle
                        bundle.putString("remember", "true");
                        bundle.putString("url", "empty");
                        bundle.putString("wsresponse", wsResponse);
                        //Add the bundle to the intent
                        intObj.putExtras(bundle);
                        if(isDataExist()){
                            deleteData();
                        }
                        insertData(username, password, url, rememberMe);
                    }
                    //Navigate to WebView Screen
                    startActivity(intObj);
                    finish();
                }else {
                    //Set Error message
                    txtResult.setText(wsResponse);
                }
                //Error status is true
            }else{
                txtResult.setText("Error occured in invoking webservice");
            }
            //Re-initialize Error Status to False
            errored = false;
        }

        @Override
        //Make Progress Bar visible
        protected void onPreExecute() {
//            progressBar.setVisibility(View.VISIBLE);
            pDialog.setMessage("Logging in ...");
            showDialog();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public void insertData(String username, String password, String url, String flRemember){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into LOGINDATA(USERNAME, PASSWORD, URL_ADDRESS, FL_REMEMBER)values('" +
                username+"','"+
                password+"','"+
                url+"','"+
                flRemember+"')");
//        Toast.makeText(getApplicationContext(), "Insert Data Success", Toast.LENGTH_LONG).show();
//        MainActivity.ma.RefreshList();
        finish();
    }

    public void deleteData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from LOGINDATA where FL_REMEMBER = 'Y'");
        finish();
    }

    public boolean isDataExist(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("select * from LOGINDATA where FL_REMEMBER = 'Y' ", null);
        String[] remember = new String[cursor.getCount()];
        cursor.moveToFirst();
        if (cursor.getCount() > 0){
            return true;
        }else {
            return false;
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
