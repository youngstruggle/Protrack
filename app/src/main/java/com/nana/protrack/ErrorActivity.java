package com.nana.protrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ErrorActivity extends AppCompatActivity {
    Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        Bundle bundle = getIntent().getExtras();
        final String url = bundle.getString("urlforerror");
        final String remember = bundle.getString("remember");

        btn1 = (Button)findViewById(R.id.button1) ;
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                Bundle bundle = new Bundle();
                //Add your data to bundle
                bundle.putString("remember", remember);
                bundle.putString("url", url);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
