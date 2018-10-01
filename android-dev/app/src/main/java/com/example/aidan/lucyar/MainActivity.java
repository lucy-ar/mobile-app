package com.example.aidan.lucyar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Replaceable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Uri mImageUri;
    private ImageView imageView;
    private String imageFilePath = "";
    private AzureWrapper azureWrapper;
    private Bitmap imageCapture;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        imageView = (ImageView) findViewById(R.id.image);

        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.send:
                            break;
                        case R.id.camera:
                            openCameraIntent();
                            break;
                        case R.id.action_cancel:
                            break;
                        case R.id.menu:
                            break;
                        case R.id.search:
                            break;
                    }
                    return true;
                }
            });
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


                //TODO: call request to do a posty boi (post request)
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
            }
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
        Call<AzureResponseHandler> callApi = azureWrapper.describe(post, "2f931d84e94b4ee3bce4190e0a984e3f", "application/octet-stream");
        callApi.enqueue(new Callback<AzureResponseHandler>() {
            @Override
            public void onResponse(Call<AzureResponseHandler> call, Response<AzureResponseHandler> response) {
                Toast.makeText(getBaseContext(), response.body().getDescription().getCaptions().get(0).getText(), Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(Call<AzureResponseHandler> call, Throwable t) {
                Toast.makeText(getBaseContext(), "connection not successful :(", Toast.LENGTH_LONG)
                        .show();
            }
        });


    }

}
