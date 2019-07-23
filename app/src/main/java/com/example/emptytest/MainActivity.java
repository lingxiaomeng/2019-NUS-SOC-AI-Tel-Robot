package com.example.emptytest;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emptytest.img.ImageProcessor;
import com.example.emptytest.img.ImageSaver;
import com.example.emptytest.yoloClassifier.Classifier;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.emptytest.LoginActivity.yolov3_classifier;
import static com.example.emptytest.yoloClassifier.untils.processBitmap;

public class MainActivity extends AppCompatActivity {
    static int filenumber = 0;
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static void saveBitmapToLocal(Bitmap bitmap) {
        filenumber++;
        try {
            // 创建文件流，指向该路径，文件名叫做fileName
            File file = new File(FILE_PATH, filenumber + ".jpg");
            // file其实是图片，它的父级File是文件夹，判断一下文件夹是否存在，如果不存在，创建文件夹
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                // 文件夹不存在
                fileParent.mkdirs();// 创建文件夹
            }
            // 将图片保存到本地
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap resized_image;
    private ImageSaver imageSaver;
    private Button refresh;
    private Button stop;
    private TextView sensor;
    private TextView result;
    //private TextView mLogLeft;
    private TextView mLogRight;
    private TextView distancelog;
    private String ip;
    Socket socket = null;
    private String send_buff = null;
    private String recv_buff = null;
    private WebView web_wiew;
    private int cal = 0;
    private ImageView imageView;
    private Button tphoto;
    private Handler mHandler = new Handler();
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
            } else if (msg.what == 3) {
                distancelog.setText("Start Send");
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        imageSaver = new ImageSaver();
        imageSaver.createFolderIfNotExist();
        //mLogLeft = (TextView) findViewById(R.id.log_left);
        tphoto = findViewById(R.id.tphoto);
        mLogRight = (TextView) findViewById(R.id.log_right);
        distancelog = findViewById(R.id.distance);
        web_wiew = (WebView) findViewById(R.id.web_view);        //这里的文件路径是死定的，把html文件名改掉就可以了
        sensor = findViewById(R.id.senor);
        refresh = findViewById(R.id.refresh);
        stop = findViewById(R.id.stop);
        result = findViewById(R.id.result);
        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);
        imageView = findViewById(R.id.imageview);
        if (getIntent() != null) {
            ip = getIntent().getStringExtra("ip");
            distancelog.setText(ip);
            socket = LoginActivity.socket;
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("stop\n");
                // recv();
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
                //recv();
                Bitmap bitmap = captureWebView(web_wiew);
                resized_image = processBitmap(bitmap, yolov3_classifier.getInputSize());
                ArrayList<Classifier.Recognition> results = null;

                results = yolov3_classifier.RecognizeImage(resized_image);

                resized_image = Bitmap.createBitmap(resized_image);
                final Canvas canvas = new Canvas(resized_image);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    System.out.println(result);
                    if (location != null && result.getConfidence() >= 0.1) {
                        canvas.drawRect(location, paint);
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(resized_image);

                    }
                });
//
//                try {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            result.setText("Start Processing");
//                        }
//                    });
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            result.setText(String.format("results: "));
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

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
                    mLogRight.setText("left speed:" + lspeed + " " + "right speed:" + rspeed);
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
                        byte[] buffer = new byte[512];
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
                    String[] dis_sensor = recv_buff.split(",");
                    Message message = new Message();
                    if (dis_sensor.length >= 4)
                        message.obj = String.format("Left: %s Right: %s Back: %s Direction: %s", dis_sensor[0], dis_sensor[1], dis_sensor[2], dis_sensor[3]);
                    else message.obj = recv_buff;
                    rechandler.sendMessage(message);
                }
            }
        }).start();
        //单开一个线程循环接收来自服务器端的消息
    }

    @SuppressLint("DefaultLocale")
    private Bitmap captureWebView(WebView webView) {
        float scale = webView.getScale();
        System.out.println(scale);
        int width = webView.getWidth();
        int height = (int) (webView.getHeight() * scale);
        System.out.println(String.format("w: %d h: %d", width, height));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
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
                        Message msg = new Message();
                        msg.what = 3;
                        sendhandler.sendMessage(msg);
                        outputStream.write(send_buff.getBytes());
                        System.out.println("send");
                        outputStream.flush();
                        msg = new Message();
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

    private static Bitmap getScaleBitmap(Bitmap bitmap, int size) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) size) / width;
        float scaleHeight = ((float) size) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}
