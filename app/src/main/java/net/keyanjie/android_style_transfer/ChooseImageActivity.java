package net.keyanjie.android_style_transfer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ChooseImageActivity extends AppCompatActivity {
    int method = 0;
    File photoFile;
    private static final int PHOTO_REQUEST_GALLERY = 0;
    private static final int PHOTO_REQUEST_CAMERA = 1;

    private FileInputStream is = null;

    Uri camera_uri;
    Uri garrery_uri;

    ImageView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        View back = findViewById(R.id.back1);
        View next = findViewById(R.id.next1);
        photoView = findViewById(R.id.uploadPhoto);
        View byLocal = findViewById(R.id.ByLocal);
        View byCamera = findViewById(R.id.ByCamera);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (method == 0) {
                    Toast toast=Toast.makeText(
                            getApplicationContext(),
                            "Please choose a photo~",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    Intent intent = new Intent(getApplicationContext(), ChooseStyleActivity.class);
                    intent.putExtra("uri", method == 1 ? camera_uri.toString() : garrery_uri.toString());
                    startActivity(intent);

                }
            }
        });

        byCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    try {
                        photoFile = createImageFile();  //创建临时图片文件，方法在下面
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        //FileProvider 是一个特殊的 ContentProvider 的子类，它使用 content:// Uri 代替了 file:///
                        // Uri. ，更便利而且安全的为另一个app分享文件
                        camera_uri = FileProvider.getUriForFile(ChooseImageActivity.this,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, camera_uri);
                        startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
                    }
                }
            }
        });

        byLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("TAH", storageDir.toString());
        //创建临时文件,文件前缀不能少于三个字符,后缀如果为空默认未".tmp"
        File image = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 文件夹 */
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_REQUEST_CAMERA) {
                try {
                    is = new FileInputStream(photoFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    photoView.setImageBitmap(bitmap);
                    method = 1;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == PHOTO_REQUEST_GALLERY) {
                if (data != null) {
                    garrery_uri = data.getData();
                    Log.d("PhotoPath", data.getData() + "");
                    photoView.setImageURI(garrery_uri);
                    method = 2;
                }
            }
        }
    }
}
