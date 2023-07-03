package com.example.myinteriorapp.network;

import com.example.myinteriorapp.activities.FurnitureList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import com.example.myinteriorapp.models.Place;
import com.example.myinteriorapp.models.ImageData;

public interface ApiInterface {
    @Multipart
    @POST("food") // 실제 API 엔드포인트를 입력해주세요.
    Call<ApiResponse<Place>> identifyPlace(@Part MultipartBody.Part photo);

    @GET("api/endpoint") // 실제 API 엔드포인트를 입력해주세요.
    Call<ApiResponse<FurnitureList>> getRecommendedFurniture(@Query("placeId") String placeId);
}

