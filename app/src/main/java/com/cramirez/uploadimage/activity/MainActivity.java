package com.cramirez.uploadimage.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.cramirez.uploadimage.R;
import com.cramirez.uploadimage.bean.Response;
import com.cramirez.uploadimage.retrofit.RetrofitInterface;
import com.cramirez.uploadimage.retrofit.ServiceGenerator;
import com.cramirez.uploadimage.util.Constants;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnSelectImage)
    Button mBtnSelectImage;
    @BindView(R.id.btnShowImage)
    Button mBtnShowImage;
    @BindView(R.id.progress)
    ProgressBar mProgressBar;

    private String mImageUrlResult = "";
    private RetrofitInterface mRetrofitApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRetrofitApi = ServiceGenerator.createService(RetrofitInterface.class);

    }

    @OnClick(R.id.btnSelectImage)
    void onClickBtnSelectImage() {
        mBtnShowImage.setVisibility(View.GONE);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/type");

        try {
            startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @OnClick(R.id.btnShowImage)
    void onClickShowImage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mImageUrlResult));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    uploadImageToServer(getBytes(inputStream));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private byte[] getBytes(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImageToServer(byte[] imageBytes) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestBody);
        Call<Response> uploadImage = mRetrofitApi.uploadImage(body);
        uploadImage.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                mProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Response responseBody = response.body();
                    mBtnShowImage.setVisibility(View.VISIBLE);
                    mImageUrlResult = Constants.HOST_NAME + responseBody.getPath();
                    Snackbar.make(findViewById(R.id.coordinatorLayoutMain),
                            responseBody.getMessage(),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Gson gson = new Gson();
                    try {
                        Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                        Snackbar.make(findViewById(R.id.coordinatorLayoutMain),
                                errorResponse.getMessage(),
                                Snackbar.LENGTH_SHORT);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Log.e(MainActivity.class.getSimpleName(), "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
