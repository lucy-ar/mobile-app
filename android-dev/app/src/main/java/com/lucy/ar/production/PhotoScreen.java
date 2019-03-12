package com.lucy.ar.production;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PhotoScreen extends AppCompatActivity {
    private String imagePath;
    private Uri photo;
    private ImageView photoTaken;
    private ImageButton share;
    private ImageButton feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_screen);
        photoTaken = findViewById(R.id.photo);
        share = findViewById(R.id.share);
        feed = findViewById(R.id.feed);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("photo");
        photo = Uri.parse(imagePath);
        photoTaken.setImageURI(photo);
        shareButton();
        addToFeed();
    }

    public void shareButton(){
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, photo);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Look at what I made with LucyAR"));
            }
        });
    }

    public void addToFeed() {
        feed.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });
    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent sceneform = new Intent(this, Sceneform.class);
//        sceneform.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(sceneform);
//    }
}
