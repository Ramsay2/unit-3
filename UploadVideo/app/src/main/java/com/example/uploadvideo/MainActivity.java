package com.example.uploadvideo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import Nework.ApiService;
import Nework.Network;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private VideoView mIvGallery;
    private Button mBtnOpenGallery;
    private Button mBtnUploadVideo;
    private String videoPath;

    private ActivityResultLauncher<Intent> launchGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getData() != null) {
                        Uri selectedVideoUri = result.getData().getData();
                        try {
                            getVideoPathFromUri(selectedVideoUri);
                            mIvGallery.setVideoURI(selectedVideoUri);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @NotNull
    private Cursor getVideoPathFromUri(Uri selectedVideo) {
        String[] filePath = {MediaStore.Video.Media.DATA};
        Cursor c = getContentResolver().query(selectedVideo, filePath,
                null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        videoPath = c.getString(columnIndex);
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mIvGallery = findViewById(R.id.videoView);
        mBtnOpenGallery = findViewById(R.id.openGallery);
        mBtnUploadVideo = findViewById(R.id.upload);
        mBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }

            }
        });
        mBtnUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToApi();
            }
        });
    }
    private void uploadImageToApi(){
        ApiService apiService = Network.getInstance().create(ApiService.class);
        File file = new File(videoPath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("video/*"),file);
        MultipartBody.Part multiBody = MultipartBody.Part.createFormData("video",file.getName(),requestBody);
        apiService.uploadImage(multiBody,"facebook").enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {

            }
        });

    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        launchGallery.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}