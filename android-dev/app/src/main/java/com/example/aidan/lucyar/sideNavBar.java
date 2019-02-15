package com.example.aidan.lucyar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class sideNavBar extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Uri mImageUri;
    private ImageView imageView;
    private String imageFilePath = "";
    private Bitmap imageCapture;
    private TextView classification;
    private DrawerLayout drawerLayout;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_nav_bar);

        Button settingButton = (Button) findViewById(R.id.settings);
        settingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment settingsPage = new SettingsPage();
                changeFragment(settingsPage);
            }
        });

//        Button shareButton = (Button) findViewById(R.id.share);
//        shareButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               Intent myIntent = new Intent(Intent.ACTION_SEND);
//               myIntent.setType("text/plain");
//               String shareBody = "Your body here";
//               String shareSub = "Your Subject here";
//               myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
//               myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
//               startActivity(Intent.createChooser(myIntent, "Share using"));
//            }
//        });
    }
    public void changeFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.drawer_layout, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
    }
}
