package com.example.aidan.lucyar;
import okhttp3.MultipartBody;
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
    @Headers({
            "Ocp-Apim-Subscription-Key:2f931d84e94b4ee3bce4190e0a984e3f",
            "Content-Type:multipart/form-data"
    })
    @Multipart
    @POST("/describe")
    Call<ResponseBody> describe (@Part MultipartBody.Part post);
}
