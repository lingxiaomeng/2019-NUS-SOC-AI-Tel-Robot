package com.example.emptytest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.emptytest.ternsorflow.Classifier;
import com.example.emptytest.ternsorflow.YoloV3Classifier;
import com.example.emptytest.yoloClassifier.*;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends Activity {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "初始化失败");
        }
    }

    private Button login;
    private EditText uname;
    static Socket socket = null;
    static Classifier yolov3_classifier;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            uname.setHint("faild");
        }
    };
    private static final String INPUT_NAME = "input_1";
    private static final String OUTPUT_NAME = "conv2d_10/BiasAdd,conv2d_13/BiasAdd";
//    private static final String INPUT_NAME = "yolov3-tiny/net1";
//    private static final String OUTPUT_NAME = "yolov3-tiny/convolutional10/BiasAdd,yolov3-tiny/convolutional13/BiasAdd";

    private static final String MODEL_FILE = "final320";
    private static final int[] TINY_YOLO_BLOCK_SIZE = {32, 16};

    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static int REQUEST_PERMISSION_CODE = 1;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    private Classifier initClassifier() {
        try {
            return YoloV3Classifier.create(
                    super.getAssets(),
                    MODEL_FILE,
                    416,
                    INPUT_NAME,
                    OUTPUT_NAME,
                    TINY_YOLO_BLOCK_SIZE, 0
            );

        } catch (Exception e) {
            throw new RuntimeException("classifier init problem", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        uname = (EditText) findViewById(R.id.uname);
        login = (Button) findViewById(R.id.login);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }


//        if (classifier == null) {
//            // 创建 Classifier
//            classifier = TensorFlowImageClassifier.create(LoginActivity.this.getAssets(),
//                    MODEL_FILE, LABEL_FILE, 320, INPUT_NAME, OUTPUT_NAME);
//        }
        if (yolov3_classifier == null) {
            yolov3_classifier = initClassifier();
        }
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