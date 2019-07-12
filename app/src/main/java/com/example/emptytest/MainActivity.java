package com.example.emptytest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emptytest.RockerView;

public class MainActivity extends AppCompatActivity {

    //private TextView mLogLeft;
    private TextView mLogRight;
    private TextView distancelog;

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
                }

                @Override
                public void distance(int distance) {
                    distancelog.setText(distance + "");
                }

                @Override
                public void angle(double angle) {
                    mLogRight.setText("摇动角度 : " + angle);
                }

                @Override
                public void onFinish() {
                    mLogRight.setText(null);
                    distancelog.setText(null);
                }
            });
        }
    }

//    private String getDirection(RockerView.Direction direction) {
//        String message = null;
//        switch (direction) {
//            case DIRECTION_LEFT:
//                message = "左";
//                break;
//            case DIRECTION_RIGHT:
//                message = "右";
//                break;
//            case DIRECTION_UP:
//                message = "上";
//                break;
//            case DIRECTION_DOWN:
//                message = "下";
//                break;
//            case DIRECTION_UP_LEFT:
//                message = "左上";
//                break;
//            case DIRECTION_UP_RIGHT:
//                message = "右上";
//                break;
//            case DIRECTION_DOWN_LEFT:
//                message = "左下";
//                break;
//            case DIRECTION_DOWN_RIGHT:
//                message = "右下";
//                break;
//            default:
//                break;
//        }
//        return message;
//    }
}