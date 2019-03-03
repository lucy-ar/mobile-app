package com.example.aidan.lucyar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.aidan.lucyar.drawar.DrawAR;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lapism.searchview.Search;
import com.lapism.searchview.database.SearchHistoryTable;
import com.lapism.searchview.widget.SearchAdapter;
import com.lapism.searchview.widget.SearchItem;
import com.lapism.searchview.widget.SearchView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;


public class Sceneform extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArFragment fragment;
    private DrawerLayout drawerLayout;
    private PointerDrawable pointer = new PointerDrawable();
    private SearchView searchView;
    private boolean isTracking;
    private boolean isHitting;
    private SearchItem suggestion;
    private List<SearchItem> suggestions;
    private SearchAdapter searchAdapter;
    private ImageButton draw;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private File gltf;
    private File bin;
    private String gltfFile = "http://10.0.2.2:5000/models/Bed_01.gltf";
    private boolean lock_gltf = false;
    private boolean lock_bin = false;
    private static String BASE_URL = "https://github.com/lucy-ar/models/raw/master/renderables/";
    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED
    }
    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private GestureDetector gestureDetector;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        toTheWindow();
        tooManySideNavs();
        FABulous();
        setupData();
        setupSearch();
        ARyouSure();
        toTheWall();
        setupDraw();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }

    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(fragment, hit.createAnchor(), model);
                    Log.d("fuck", "second part add object: ");
                    break;

                }
            }
        }

    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        ModelRenderable.builder()
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        model,
                        RenderableSource.SourceType.GLTF2)
//                        .setScale(0.01f)  // Scale the original model to 50%.
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())
                .setRegistryId(model.toString())
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally(
                throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load renderable " +
                                    model.toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
        node.setOnTouchListener(new Node.OnTouchListener() {
            @Override
            public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.login:
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent loginPage = new Intent(Sceneform.this, LoginActivity.class);
                        loginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginPage);
                        finish();
                    }
                });
                break;
            case R.id.gallery:
                break;
            case R.id.settings:
                break;
        }
        return true;
    }

    public void setupData() {
        suggestions = new ArrayList<>();

        try {
            JSONArray furnitureArray = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i < furnitureArray.length(); i++) {
                suggestion = new SearchItem(this);
                JSONObject params = furnitureArray.getJSONObject(i);
                suggestion.setTitle(params.getString("furniture"));
                suggestion.setSubtitle(params.getString("gltf_file"));
                suggestions.add(suggestion);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("huh", "setupData: ");
        }

    }

    public void setupSearch() {
        searchView = (SearchView) findViewById(R.id.searchView);
        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this);

        searchAdapter = new SearchAdapter(this);
        searchAdapter.setSuggestionsList(suggestions);
        searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
            @Override
            public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle) {
                SearchItem item = new SearchItem(Sceneform.this);
                item.setTitle(title);
                item.setSubtitle(subtitle);
                addObject(Uri.parse(BASE_URL + subtitle.toString()));
                searchView.setText(title.toString());
                mHistoryDatabase.addItem(item);
            }
        });

        searchView.setAdapter(searchAdapter);
        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                SearchItem item = new SearchItem(Sceneform.this);
                item.setTitle(query);
                mHistoryDatabase.addItem(item);
                return true;
            }
            @Override
            public void onQueryTextChange(CharSequence newText) {
            }
        });

        searchView.setOnOpenCloseListener(new Search.OnOpenCloseListener() {
            @Override
            public void onOpen() {
                searchView.setBackgroundColor(getColor(R.color.white));
                searchView.setLogoColor(getColor(R.color.gray));
                searchView.setTextColor(getColor(R.color.gray));
                searchView.setHintColor(getColor(R.color.gray));
                searchView.setText("");
            }

            @Override
            public void onClose() {
                searchView.setBackgroundColor(getColor(R.color.transparent));
                searchView.setLogoColor(getColor(R.color.white));
                searchView.setTextColor(getColor(R.color.white));
                searchView.setHintColor(getColor(R.color.white));
            }
        });

        searchView.setOnLogoClickListener(new Search.OnLogoClickListener() {
            @Override
            public void onLogoClick() {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
    }

    public void FABulous() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingAction);
        fab.setOnClickListener(view -> takePhoto());
    }

    public void tooManySideNavs() {
        drawerLayout = (DrawerLayout) findViewById(R.id.sceneform_drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.sceneform_nav);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void toTheWindow() {
        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public void toTheWall() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void ARyouSure() {
        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("huh", "loadJSONFromAsset: ");
            return null;
        }
        return json;
    }

    public void setupDraw() {
        draw = (ImageButton) findViewById(R.id.ar_draw);
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent arDraw = new Intent(Sceneform.this, DrawAR.class);
                startActivity(arDraw);
            }
        });
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }


    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {
        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = fragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(Sceneform.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                File photoFile = new File(filename);

                Uri photoURI = FileProvider.getUriForFile(Sceneform.this,
                    Sceneform.this.getPackageName() + ".ar.codelab.name.provider",
                    photoFile);
                Intent intent = new Intent(this, PhotoScreen.class);
                intent.putExtra("photo", photoURI.toString());
                intent.removeFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(Sceneform.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }
}
