package com.example.aidan.lucyar;
import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AzureComputeResponse {
    private byte[] byteRequest;
    @SerializedName("tags")
    private ArrayList<String> tags = new ArrayList<String>();

    private byte[] getImageBytes(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return baos.toByteArray();
    }

    public AzureComputeResponse(Bitmap imageRequest) {
        this.byteRequest = getImageBytes(imageRequest);
    }

    public ArrayList<String> getTags() {
        return tags;
    }
}
