package net.keyanjie.android_style_transfer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompleteActivity extends AppCompatActivity {

    private static String INPUT_NODE = "input";
    private static String OUTPUT_NODE = "output_new";

    private int[] intValues;
    private float[] floatValues;
    String MODEL_FILE;

    ProgressDialog dialog = null;
    Bitmap final_photo;
    ImageView photoView;
    View download;
    View back_to_home;
    View back;

    private TensorFlowInferenceInterface inferenceInterface;
    double radio = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        photoView = findViewById(R.id.complete);

        Intent intent = getIntent();
        String strUri = intent.getStringExtra("uri");
        int style_pos = intent.getIntExtra("style", 0);

        Uri uri = Uri.parse(strUri);

        back = findViewById(R.id.back3);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });


        String[] pu_list = new String[]{
                "cubist",
                "feathers",
                "ink",
                "la_muse",
                "mosaic",
                "scream",
                "starry",
                "udnie",
                "wave"
        };

        MODEL_FILE = "file:///android_asset/" + pu_list[style_pos] + ".pb";

        if (pu_list[style_pos].equals("la_muse")) {
            OUTPUT_NODE = "output";
        }

        try {
            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            radio = (double)photo.getWidth() / (double)photo.getHeight();

            StylizeTask stylizeTask = new StylizeTask();
            stylizeTask.execute(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        download = findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {

                    int hasReadContactsPermission = CompleteActivity.this.checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {

                        CompleteActivity.this.requestPermissions(
                                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);

                        return;
                    }
                    if (final_photo == null) {
                        System.out.println("final_photo == null");
                    }
                    saveMyBitmap(final_photo);

                } else {
                    //在低版本中直接调用该函数
                    saveMyBitmap(final_photo);
                }

            }
        });

        back_to_home = findViewById(R.id.back_to_home);
        back_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CompleteActivity.this, ChooseImageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    public void saveMyBitmap(Bitmap mBitmap) {

        File file = new File(Environment.getExternalStorageDirectory().getPath() + "style_transfer");
        if (!file.exists())
            file.mkdir();

        file = new File(Environment.getExternalStorageDirectory().getPath() + "temp.jpg".trim());
        String fileName = file.getName();
        System.out.println(fileName);
        String sName = fileName.substring(fileName.lastIndexOf("."));
        System.out.println("sName " + sName);

        String newFilePath = Environment.getExternalStorageDirectory().getPath() + "style_transfer/" + "Stylized" + System.currentTimeMillis() + sName;
        file = new File(newFilePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(getApplicationContext(),"Save to " + newFilePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class StylizeTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private StylizeTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = ProgressDialog.show(CompleteActivity.this, "", "Style transfering...");
                dialog.show();
            }
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {

            initTensorFlowAndLoadModel();
            return stylizeImage(bitmaps[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        public String toString() {
            return "StylizeTask{}";
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.d("11111radio", "" + radio);
            Log.d("11111weidth", "" + width);
            Log.d("11111height", "" + height);
            if (width / height > radio) {
                bitmap = scaleBitmap(bitmap, (int)(height * radio), height);
            } else {
                bitmap = scaleBitmap(bitmap, width, (int)(width / radio));
            }
            Log.d("11111weidth", "" + bitmap.getWidth());
            Log.d("11111height", "" + bitmap.getHeight());
            final_photo = bitmap;
            photoView.setImageBitmap(bitmap);
        }

        private void initTensorFlowAndLoadModel() {
            intValues = new int[640 * 480];
            floatValues = new float[640 * 480 * 3];
            inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);
        }

        private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
            if (origin == null) {
                return null;
            }
            int height = origin.getHeight();
            int width = origin.getWidth();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            if (!origin.isRecycled()) {
                origin.recycle();
            }
            return newBM;
        }

        private Bitmap stylizeImage(Bitmap bitmap) {
            Bitmap scaledBitmap = scaleBitmap(bitmap, 480, 640); // desiredSize
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight());
            for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];
                floatValues[i * 3] = ((val >> 16) & 0xFF) * 1.0f;
                floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) * 1.0f;
                floatValues[i * 3 + 2] = (val & 0xFF) * 1.0f;
            }

            // Copy the input data into TensorFlow.
            inferenceInterface.feed(INPUT_NODE, floatValues, 640, 480, 3);
            // Run the inference call.
            inferenceInterface.run(new String[]{OUTPUT_NODE});
            // Copy the output Tensor back_light into the output array.
            inferenceInterface.fetch(OUTPUT_NODE, floatValues);

            for (int i = 0; i < intValues.length; ++i) {
                intValues[i] =
                        0xFF000000
                                | (((int) (floatValues[i * 3])) << 16)
                                | (((int) (floatValues[i * 3 + 1])) << 8)
                                | ((int) (floatValues[i * 3 + 2]));
            }
            scaledBitmap.setPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight());
            return scaledBitmap;
        }
    }
}
