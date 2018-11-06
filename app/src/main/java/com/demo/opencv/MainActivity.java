package com.demo.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");

    }

    //最大
    private double max_size = 1024;
    //回调
    private int PICK_IMAGE_REQUEST = 1;

    //原图， 处理后的图片
    private ImageView mImgOriginal, mImgDeals;
    //原始Bitmap、处理后的Bitmap
    private Bitmap mOriginalBitmap, mDealBitmap;
    //选择图片、处理
    private Button mBtnChoose, mBtnDeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onLoadOpenCVLibrary();
        initView();
    }


    /**
     * OpenCV库静态加载并初始化
     */
    private void onLoadOpenCVLibrary() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.e("CV", "Open CV Libraries loaded...");
        }
    }


    private void initView() {
        mImgDeals = findViewById(R.id.img_deals);
        mImgOriginal = findViewById(R.id.img_original);
        mBtnChoose = findViewById(R.id.btn_choose);
        mBtnDeals = findViewById(R.id.btn_deals);

        mBtnDeals.setOnClickListener(this);
        mBtnChoose.setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_choose: {//选择
                selectImage();
                break;
            }
            case R.id.btn_deals: {//处理
                convertGray();
                break;
            }
        }


    }

    //选择图片
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择图像"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Log.e("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if (max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while ((newWidth / inSampleSize) > max_size || (newHeight / inSampleSize) > max_size) {
                        inSampleSize *= 2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                mOriginalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
                        null, options);
                mDealBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
                        null, options);
                mImgOriginal.setImageBitmap(mOriginalBitmap);




            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void convertGray() {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(mDealBitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, mDealBitmap);
        mImgDeals.setImageBitmap(mDealBitmap);
    }
}
