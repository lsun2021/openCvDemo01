首先安装好NDK,  File-> Setting -Android SDK -> SDK Tools->NDK
![配置](https://upload-images.jianshu.io/upload_images/1869441-e893a0904c1127d4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)



步骤：
1.创建一个项目，勾选 Include C++ support,然后一路next 下去
![创建Android 项目](https://upload-images.jianshu.io/upload_images/1869441-cfd5aca7b4ea1558.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)

2. 最后勾选 C++。等待项目创建完成。（勾选C++ 是因为需要用到C++的配置）
![创建Android 项目](https://upload-images.jianshu.io/upload_images/1869441-d8dc31411b12fc64.png?imageMogr2/auto-orient/strip%7CimageView2/3/w/680)


3.接下来是导入model 项目，添加项目依赖包。方便大家，我直接放在我的云盘上。
链接：https://pan.baidu.com/s/1sH9PvgoZFI3Igy5knUY1MA 
提取码：x5e0 


![导入项目依赖](https://upload-images.jianshu.io/upload_images/1869441-0b510ee549adf895.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)

![导入项目依赖](https://upload-images.jianshu.io/upload_images/1869441-32cb6609d4a1a881.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)


4. 然后添加项目依赖， File - > Project Structure -> Dependencies - >Module dependency

![添加依赖](https://upload-images.jianshu.io/upload_images/1869441-a47a9fa2ef631782.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)

5.  选择  openCVLibrary  添加依赖
![完成依赖](https://upload-images.jianshu.io/upload_images/1869441-ddd4e14823a4e9fe.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)

注意：依赖的项目包可能和你的项目的版本不一致，你更新一下就好了。

![注意事项](https://upload-images.jianshu.io/upload_images/1869441-425b8cad9bef1e08.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/680)

到这里，基本的OpenCV就配置好了。接下来就是项目的编写。用于检验配置是否完成。

实现的项目效果：
1.从相册中选择一张照片，然后进行灰度处理。

activity_layout.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <Button
            android:id="@+id/btn_choose"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="10dp"
            android:text="原图"
            android:textSize="16sp" />

        <Button
            android:id="@+id/btn_deals"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="10dp"
            android:text="处理"
            android:textSize="16sp" />

    </LinearLayout>

    <ImageView
        android:id="@+id/img_original"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_gravity="center"
        android:layout_margin="16dp"
        android:scaleType="fitCenter"
        android:src="@mipmap/ic_launcher" />

    <ImageView
        android:id="@+id/img_deals"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_gravity="center"
        android:layout_margin="16dp"
        android:scaleType="fitCenter"
        android:src="@mipmap/ic_launcher" />
</LinearLayout>


```

MainActivity.java
```
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

```

最终实现效果：
![实现效果](https://upload-images.jianshu.io/upload_images/1869441-d35d8da56ed1ba15.gif?imageMogr2/auto-orient/strip)

github地址：https://github.com/wangxin3119/openCvDemo01

























