package com.example.aidan.lucyar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.aidan.lucyar.MainActivity.REQUEST_IMAGE;

public class bottomAppBar extends AppCompatActivity implements SearchPage.OnFragmentInteractionListener, SettingsPage.OnFragmentInteractionListener{
    private FragmentManager fragmentManager;
    private Uri mImageUri;
    private ImageView imageView;
    private String imageFilePath = "";
    private AzureWrapper azureWrapper;
    private Bitmap imageCapture;
    private TextView classification;
    private DrawerLayout drawerLayout;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bottom_bar);
        this.imageView = (ImageView) findViewById(R.id.image);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        BottomAppBar bar = (BottomAppBar) findViewById(R.id.bar);
        setSupportActionBar(bar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.START);
            }

        });

        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.fab);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraIntent();
            }
        });
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.share_bottom:

                        break;
                    case R.id.action_search:
                        Fragment searchPage = new SearchPage();
                        changeFragment(searchPage);
                        break;
                    case R.id.settings:
                        Fragment settingsPage = new SettingsPage();
                        changeFragment(settingsPage);
                        break;
                    case R.id.login:
                        Intent loginPage = new Intent(bottomAppBar.this, LoginActivity.class);
                        startActivity(loginPage);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);


        return super.onCreateOptionsMenu(menu);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                File file = new File(imageFilePath);
                try {
                    InputStream in = new FileInputStream(file);
                    byte[] buf;
                    buf = new byte[in.available()];
                    while (in.read(buf) != -1);
                    RequestBody body = RequestBody
                            .create(MediaType.parse("application/octet-stream"), buf);
                    sendNetworkRequest(body);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageView.setImageURI(Uri.parse(imageFilePath));
                imageCapture = BitmapFactory.decodeFile(imageFilePath);

            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName() +".provider", photoFile);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(pictureIntent, REQUEST_IMAGE);
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }
    public void sendNetworkRequest(RequestBody post) {
        String AZURE_ENDPOINT = "https://canadacentral.api.cognitive.microsoft.com/vision/v1.0/";
        Retrofit.Builder azureBuilder = new Retrofit.Builder()
                .baseUrl(AZURE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit azureApiCall = azureBuilder.build();
        azureWrapper = azureApiCall.create(AzureWrapper.class);
        Call<AzureResponseHandler> callApi = azureWrapper.describe(post, "cd6ab652d78346bf97425273c77f1f5a", "application/octet-stream");
        callApi.enqueue(new Callback<AzureResponseHandler>() {
            @Override
            public void onResponse(Call<AzureResponseHandler> call, Response<AzureResponseHandler> response) {
                Toast.makeText(getBaseContext(), response.body().getDescription().getCaptions().get(0).getText(), Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(Call<AzureResponseHandler> call, Throwable t) {
                Toast.makeText(getBaseContext(), "connection not successful :(" + t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });

    public void changeFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.drawer_layout, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
    }
}
