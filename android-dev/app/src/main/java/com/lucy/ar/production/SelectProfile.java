package com.lucy.ar.production;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SelectProfile extends AppCompatActivity {
    private Button tenant;
    private Button furniture;
    private Typeface face;
    private TextView title;
    private TextView iam;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profile);
        tenant = findViewById(R.id.tenant);
        furniture = findViewById(R.id.furniture);
        title = findViewById(R.id.profile_title_text);
        iam = findViewById(R.id.iam);
        face = Typeface.createFromAsset(getAssets(), "montserratlight.ttf");
        tenant.setTypeface(face);
        furniture.setTypeface(face);
        iam.setTypeface(face);
        title.setTypeface(face);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent login = new Intent(SelectProfile.this, Sceneform.class);
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(login);
            finish();
        }

        tenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(SelectProfile.this, Sceneform.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                finish();
            }
        });

        furniture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
