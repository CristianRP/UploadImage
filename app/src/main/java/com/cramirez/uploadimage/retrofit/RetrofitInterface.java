package com.cramirez.uploadimage.retrofit;

import com.cramirez.uploadimage.bean.Response;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by cramirez on 29/12/2017.
 */

public interface RetrofitInterface {

    @Multipart
    @POST("/images/upload")
    Call<Response> uploadImage(@Part MultipartBody.Part image);
}
