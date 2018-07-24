package com.nana.protrack;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;
    Boolean isRemember = false;
    DataHelper dbcenter;
    String[] remember;
    public static MainActivity ma;
    protected Cursor cursor, cursorRemember;
    DataHelper dbHelper;
    String wsResponse = "0";
    String urlAddress, userName, userPassword;
    static Boolean errored = false;

    Handler splashHandler = new Handler();
    Runnable splashRunnable = new Runnable() {
        @Override
        public void run() {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            Bundle bundle = new Bundle();
            //Add your data to bundle
            bundle.putString("remember", "false");
            bundle.putString("url", "https://estim.co.id:8085/protrack/");
            bundle.putString("wsresponse", "empty");
            //Add the bundle to the intent
            loginIntent.putExtras(bundle);

            startActivity(loginIntent);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ma = this;
        dbcenter = new DataHelper(this);
        isRemember = isRemember();
        dbHelper = new DataHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("select URL_ADDRESS, USERNAME, PASSWORD from LOGINDATA WHERE FL_REMEMBER='Y'", null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            urlAddress=cursor.getString(0).toString();
            userName=cursor.getString(1).toString();
            userPassword=cursor.getString(2).toString();
        }
        if(isRemember){
            AsyncCallWS task = new AsyncCallWS();
            task.execute();
        }else{
            splashHandler.postDelayed(splashRunnable,SPLASH_TIME_OUT);
        }
//        splashHandler.postDelayed(splashRunnable,SPLASH_TIME_OUT);

    }

    public boolean isRemember(){
        SQLiteDatabase db = dbcenter.getReadableDatabase();
        cursorRemember = db.rawQuery("select * from LOGINDATA where FL_REMEMBER = 'Y' ", null);
        remember = new String[cursorRemember.getCount()];
        cursorRemember.moveToFirst();
        System.out.println("Estim remember me count "+cursorRemember.getCount());
        if (cursorRemember.getCount() > 0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Call Web Method
            wsResponse = WebServices.invokeLoginWS(userName,userPassword,urlAddress,"loginauthentication");
            return null;
        }

        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {
            //Make Progress Bar invisible
            Intent intObj = new Intent(MainActivity.this,WebViewActivity.class);
            //Error status is false
            if(!errored){
                //Based on Boolean value returned from WebService
                if(wsResponse != null && wsResponse.equalsIgnoreCase("1") || wsResponse != null && wsResponse.equalsIgnoreCase("2")){

                    Bundle bundle = new Bundle();
                    //Add your data to bundle
                    bundle.putString("remember", "true");
                    bundle.putString("url", "empty");
                    bundle.putString("wsresponse", wsResponse);
                    //Add the bundle to the intent
                    intObj.putExtras(bundle);
                    //Navigate to WebView Screen
                    startActivity(intObj);
                    finish();
                }else {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                    //Error status is true
                }
            }
//            else{
//                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(loginIntent);
//                finish();
//            }
            //Re-initialize Error Status to False
            errored = false;
        }

        @Override
        //Make Progress Bar visible
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
