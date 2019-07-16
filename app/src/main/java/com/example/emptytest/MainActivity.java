package com.example.emptytest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private Button refresh;
    private Button stop;
    private TextView sensor;
    //private TextView mLogLeft;
    private TextView mLogRight;
    private TextView distancelog;
    private String ip;
    Socket socket = null;
    private String send_buff = null;
    private String recv_buff = null;
    private WebView web_wiew;
    private int cal = 0;
    private Button tphoto;
    @SuppressLint("HandlerLeak")
    private Handler sendhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                distancelog.setText("Send succeed");
            } else if (msg.what == 1) {
                distancelog.setText("Getconnect fail");
            } else if (msg.what == 2) {
                distancelog.setText("Send fail");
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler updateweb = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            web_wiew.reload();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler rechandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            System.out.println(msg.obj);
            sensor.setText((String) msg.obj);
        }
    };

    @Override
    public void onBackPressed() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mLogLeft = (TextView) findViewById(R.id.log_left);
        tphoto = findViewById(R.id.tphoto);
        mLogRight = (TextView) findViewById(R.id.log_right);
        distancelog = findViewById(R.id.distance);
        web_wiew = (WebView) findViewById(R.id.web_view);        //这里的文件路径是死定的，把html文件名改掉就可以了
        sensor = findViewById(R.id.senor);
        refresh = findViewById(R.id.refresh);
        stop = findViewById(R.id.stop);
        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);

        if (getIntent() != null) {
            ip = getIntent().getStringExtra("ip");
            distancelog.setText(ip);
            socket = LoginActivity.socket;
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("stop\n");
                recv();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateweb.sendMessage(new Message());
            }
        });


        web_wiew.loadUrl("http://" + ip + ":8000/index.html");
        web_wiew.getSettings().setJavaScriptEnabled(true);
        web_wiew.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        web_wiew.getSettings().setLoadWithOverviewMode(true);
        web_wiew.getSettings().setBuiltInZoomControls(true);
        web_wiew.getSettings().setSupportZoom(true);
        web_wiew.getSettings().setUseWideViewPort(true);
        web_wiew.getSettings().setDefaultTextEncodingName("utf-8");
        tphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("photo\n");
            }
        });
        if (rockerViewRight != null) {
            rockerViewRight.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                @Override
                public void onStart() {
                    //updateweb.sendMessage(new Message());
                    mLogRight.setText(null);
                    send("stop\n");
                    recv();
                    //send("0");
                }

                @Override
                public void sendmessage(int lspeed, int rspeed) {
                    mLogRight.setText("lspeed:" + lspeed + "rspeed" + rspeed);
                    send(lspeed + "," + rspeed + "\n");
                    recv();
                }

                @Override
                public void onFinish() {
                    mLogRight.setText(null);
                    send("stop\n");
                    recv();
                }
            });
        }
    }

    private void recv() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (inputStream != null) {
                    try {
                        byte[] buffer = new byte[1024];
                        int count = inputStream.read(buffer);//count是传输的字节数
                        recv_buff = new String(buffer, StandardCharsets.US_ASCII);//socket通信传输的是byte类型，需要转为String类型
                        System.out.println("received");
                        System.out.println(recv_buff);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //将受到的数据显示在TextView上
                if (recv_buff != null) {
                    Message message = new Message();
                    message.obj = recv_buff;
                    rechandler.sendMessage(message);
                }
            }
        }).start();
        //单开一个线程循环接收来自服务器端的消息

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
                    Message msg = new Message();
                    msg.what = 1;
                    sendhandler.sendMessage(msg);
                    e.printStackTrace();
                }
                if (outputStream != null) {
                    try {
                        outputStream.write(send_buff.getBytes());
                        System.out.println("send");
                        outputStream.flush();
                        Message msg = new Message();
                        msg.what = 0;
                        sendhandler.sendMessage(msg);
                    } catch (IOException e) {
                        Message msg = new Message();
                        msg.what = 2;
                        sendhandler.sendMessage(msg);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
