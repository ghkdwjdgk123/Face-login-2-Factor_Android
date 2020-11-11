package com.example.final_project_ui_example;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoActivity extends Activity {
    final private static String TAG = "haerin";
    final private static int OTP_ACTIVITY = 500;
    Button btn_photo;
    Button btn_ok;
    ImageView iv_photo;
    final static int TAKE_PICTURE = 1;
    String byteArray;

    String mCurrentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        iv_photo = findViewById(R.id.iv_photo);
        btn_photo = findViewById(R.id.btn_photo);
        btn_ok = findViewById(R.id.btn_send);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(PhotoActivity.this, new
                        String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_photo:
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, TAKE_PICTURE);
                        dispatchTakePictureIntent();
                        break;
                }
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_send:
                        Toast.makeText(PhotoActivity.this, "당신의 바이트는 " + byteArray, Toast.LENGTH_SHORT).show();
                        Intent otp_intent = new Intent(PhotoActivity.this, Otp_Activity.class);
                        startActivityForResult(otp_intent, OTP_ACTIVITY);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
        }
    }

    public String bitmapToByteArray(Bitmap bitmapPicture) {
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
//    private String getStringFromBitmap(Bitmap bitmapPicture) {
//    String encodedImage;
//    ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
//    bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
//    byte[] b = byteArrayBitmapStream.toByteArray();
//    encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
//    return encodedImage;
//}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        //                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap;
//                        = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        if (Build.VERSION.SDK_INT >= 29) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                            try {
                                bitmap = ImageDecoder.decodeBitmap(source);
                                if (bitmap != null) {
                                    iv_photo.setImageBitmap(bitmap);
                                    byteArray = bitmapToByteArray(bitmap);
                                    Log.d(TAG, "당신의 바이트는 " + byteArray);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                                if (bitmap != null) {
                                    iv_photo.setImageBitmap(bitmap);
                                    byteArray = bitmapToByteArray(bitmap);
                                    Log.d(TAG, "당신의 바이트는 " + byteArray);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
    //  촬영된 사진을 이미지 파일로 저장하는 함수 createImageFile()
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try { photoFile = createImageFile(); }
            catch (IOException ex) {}
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.final_project_ui_example.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }
}