package com.example.aidan.lucyar;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.lapism.searchview.Search;
import com.lapism.searchview.database.SearchHistoryTable;
import com.lapism.searchview.widget.SearchAdapter;
import com.lapism.searchview.widget.SearchItem;
import com.lapism.searchview.widget.SearchView;


import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);
        toTheWindow();
        tooManySideNavs();
        FABulous();
        setupData();
        setupSearch();
        ARyouSure();
        toTheWall();
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

    public void setupData() {
        suggestions = new ArrayList<>();

        suggestion = new SearchItem(this);
        suggestion.setTitle("Chair");
        suggestion.setIcon1Resource(R.drawable.wooden_chair);
        suggestion.setSubtitle("CHAHIN_WOODEN_CHAIR.sfb");
        suggestions.add(suggestion);

        suggestion = new SearchItem(this);
        suggestion.setTitle("Desk");
        suggestion.setIcon1Resource(R.drawable.desk);
        suggestion.setSubtitle("Desk.sfb");
        suggestions.add(suggestion);

        suggestion = new SearchItem(this);
        suggestion.setTitle("Lamp");
        suggestion.setIcon1Resource(R.drawable.desk);
        suggestion.setSubtitle("lamp.sfb");
        suggestions.add(suggestion);
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

                addObject(Uri.parse(subtitle.toString()));

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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

}
