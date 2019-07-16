package com.example.emptytest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends Activity {
    private Button login;
    private EditText uname;
    static Socket socket = null;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            uname.setHint("faild");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        uname = (EditText) findViewById(R.id.uname);
        login = (Button) findViewById(R.id.login);

        login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final String str1 = uname.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (socket != null) {
                                socket.close();
                                socket = null;
                            }
                            socket = new Socket(str1, 7654);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("ip", str1);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                            uname.setHint("faild");
                        }
                    }
                }).start();
            }
        });
    }

}