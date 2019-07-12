package com.example.emptytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
    private Button login;
    private EditText uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        uname = (EditText) findViewById(R.id.uname);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final String str1 = uname.getText().toString();
                if (str1.equals("username")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    uname.setText("失败");
                }
            }
        });
    }

}