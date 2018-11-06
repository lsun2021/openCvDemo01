package com.demo.opencv;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Author ： MemoThree
 * Time   ： 2018/11/6
 * Desc   ：
 */
public class MainActivity2 extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private ImageView iv_image;
    private Button bt_test;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_main);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        bt_test = (Button) findViewById(R.id.bt_test);
        final Bitmap bitmap =((BitmapDrawable)getResources().getDrawable(R.drawable.de)).getBitmap();
        iv_image.setImageBitmap(bitmap);
        bt_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap grayBitmap = toGrayByOpencv(bitmap);
                iv_image.setImageBitmap(grayBitmap);
            }
        });
    }

    /**
     * 灰度化
     * @param srcBitmap
     * @return
     */
    public Bitmap toGrayByOpencv(Bitmap srcBitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(srcBitmap,mat);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGRA2GRAY, 1);
        Utils.matToBitmap(grayMat,srcBitmap);
        return srcBitmap;
    }

}
