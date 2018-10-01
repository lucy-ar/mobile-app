package com.example.aidan.lucyar;
import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AzureResponseHandler {

    @SerializedName("description")
    @Expose
    private Description description;
    @SerializedName("requestId")
    @Expose
    private String requestId;
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

}

