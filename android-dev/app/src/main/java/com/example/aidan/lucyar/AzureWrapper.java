package com.example.aidan.lucyar;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AzureWrapper {
    @POST("describe")
    Call<AzureResponseHandler> describe (@Body RequestBody post,
                                 @Header("Ocp-Apim-Subscription-Key") String key,
                                 @Header("Content-Type") String content);
}
