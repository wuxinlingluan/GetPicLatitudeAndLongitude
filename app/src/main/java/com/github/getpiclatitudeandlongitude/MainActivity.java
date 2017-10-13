package com.github.getpiclatitudeandlongitude;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.album.Album;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.bt)
    Button bt;
    @InjectView(R.id.iv)
    ImageView iv;
    @InjectView(R.id.tv)
    TextView tv;
    private ArrayList<String> pathList;
    private String picPath;
    private Bitmap bitmap;
    private float output1;
    private float output2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.bt)
    public void onViewClicked() {
        Album.album(this)
                .selectCount(1) // 最多选择几张图片。
                .camera(true) // 是否有拍照功能。
                .start(999); // 999是请求码，返回时onActivityResult()的第一个参数。
        tv.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 999) {
            if (resultCode == RESULT_OK) { // Successfully.
                // 不要质疑你的眼睛，就是这么简单。
                pathList = Album.parseResult(data);
                picPath = pathList.get(0);
                bitmap = BitmapFactory.decodeFile(picPath);
                iv.setImageBitmap(this.bitmap);
                getPhotoLocation(picPath);
            } else if (resultCode == RESULT_CANCELED) { // User canceled.
                // 用户取消了操作。
            }
        }
    }

    private void getPhotoLocation(String picPath) {

        try {
            ExifInterface exifInterface = new ExifInterface(picPath);
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngRef = exifInterface.getAttribute
                    (ExifInterface.TAG_GPS_LONGITUDE_REF);

            if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
                try {
                    output1 = convertRationalLatLonToFloat(latValue, latRef);
                    output2 = convertRationalLatLonToFloat(lngValue, lngRef);
                    tv.setText("经纬度为:"+output1+","+output2);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "照片未有经纬度信息,请打开GPS重新拍照", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static float convertRationalLatLonToFloat(String rationalString, String ref) {

        String[] parts = rationalString.split(",");

        String[] pair;
        pair = parts[0].split("/");
        double degrees = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        pair = parts[1].split("/");
        double minutes = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        pair = parts[2].split("/");
        double seconds = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
        if ((ref.equals("S") || ref.equals("W"))) {
            return (float) -result;
        }
        return (float) result;
    }

}
