package com.example.emptytest;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emptytest.RockerView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    //private TextView mLogLeft;
    private TextView mLogRight;
    private TextView distancelog;
    private String ip;
    Socket socket = null;
    private String send_buff = null;
    private String recv_buff = null;
    private WebView wv_produce;
    private int cal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mLogLeft = (TextView) findViewById(R.id.log_left);
        mLogRight = (TextView) findViewById(R.id.log_right);
        distancelog = findViewById(R.id.distance);

        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);
        if (rockerViewRight != null) {
            rockerViewRight.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                @Override
                public void onStart() {
                    mLogRight.setText(null);
                    distancelog.setText(null);
                    send("stop\n");
                    //send("0");
                }

                @Override
                public void sendmessage(int lspeed, int rspeed) {
                    mLogRight.setText("lspeed:" + lspeed + "rspeed" + rspeed);
                    send(lspeed + "," + rspeed + "\n");
                }

                @Override
                public void onFinish() {
                    mLogRight.setText(null);
                    distancelog.setText(null);
                    send("stop\n");
                }
            });
        }
    }

    private void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send_buff = message;
                //向服务器端发送消息
                System.out.println("------------------------");
                OutputStream outputStream = null;
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (outputStream != null) {
                    try {
                        outputStream.write(send_buff.getBytes());
                        System.out.println("send");
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
