package com.example.aidan.lucyar;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Sceneform extends AppCompatActivity implements BaseFrag.BaseFragmentCallbacks, NavigationView.OnNavigationItemSelectedListener {
    private ArFragment fragment;
    private DrawerLayout drawerLayout;
    private PointerDrawable pointer = new PointerDrawable();
    private FloatingSearchView mSearchView;
    private boolean isTracking;
    private boolean isHitting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);
        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        drawerLayout = (DrawerLayout) findViewById(R.id.sceneform_drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.sceneform_nav);
        navigationView.setNavigationItemSelectedListener(this);
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        onAttachSearchViewToDrawer(mSearchView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingAction);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        initializeGallery();
//        setFullscreen(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    private void initializeGallery() {
//        LinearLayout gallery = findViewById(R.id.gallery_layout);
//
//        ImageView chair = new ImageView(this);
//        chair.setImageResource(R.drawable.fridge);
//        chair.setContentDescription("chair");
//        chair.setOnClickListener(view ->{addObject(Uri.parse("Refrigerator_01.sfb"));});
//        gallery.addView(chair);
//
//        ImageView desk = new ImageView(this);
//        desk.setImageResource(R.drawable.desk);
//        desk.setContentDescription("desk");
//        desk.setOnClickListener(view ->{addObject(Uri.parse("Desk.sfb"));});
//        gallery.addView(desk);
//
//        ImageView table = new ImageView(this);
//        table.setImageResource(R.drawable.table);
//        table.setContentDescription("table");
//        table.setOnClickListener(view ->{addObject(Uri.parse("Table_01.sfb"));});
//        gallery.addView(table);
//
//        ImageView couch = new ImageView(this);
//        couch.setImageResource(R.drawable.sofa);
//        couch.setContentDescription("couch");
//        couch.setOnClickListener(view ->{addObject(Uri.parse("Couch.sfb"));});
//        gallery.addView(couch);
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
                    break;

                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    @Override
    public void onAttachSearchViewToDrawer(FloatingSearchView searchView) {
        drawerLayout = (DrawerLayout) findViewById(R.id.sceneform_drawer);
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
//            case R.id.share:
//                Intent myIntent = new Intent(Intent.ACTION_SEND);
//                myIntent.setType("text/plain");
//                String shareBody = "Your body here";
//                String shareSub = "Your Subject here";
//                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
//                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
//                startActivity(Intent.createChooser(myIntent, "Share using"));
//                break;
            case R.id.login:
                Intent loginPage = new Intent(Sceneform.this, LoginActivity.class);
                startActivity(loginPage);
                break;
            case R.id.gallery:
                break;
            case R.id.settings:
                break;
        }
        return true;
    }

    private void setFullscreen(boolean fullscreen)
    {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen)
        {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        else
        {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }
}
