package com.nana.protrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "login_crm.db";
    private static final int DATABASE_VERSION = 1;
    public DataHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String sqlCreate = "create table LOGINDATA(LOGIN_ID integer primary key autoincrement not null, URL_ADDRESS text null,USERNAME text null, " +
                "PASSWORD text null, FL_REMEMBER text null);";
        Log.d("Data", "onCreate" +sqlCreate);
        db.execSQL(sqlCreate);

    }
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2){

    }
}
