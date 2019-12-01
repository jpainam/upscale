package com.copell.upscale;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;


import com.copell.upscale.model.Category;
import com.copell.upscale.model.Product;
import com.copell.upscale.utils.Converter;
import com.copell.upscale.utils.ItemClickSupport;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements
        DiscountFragment.OnFragmentInteractionListener,
        InventoryFragment.OnFragmentInteractionListener {

    private static int cart_count=0;
    private static final String TAG = "MainActivity";
    Account mAccount;
    ViewPager viewPager;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @BindView(R.id.lv_root_categories) RecyclerView lv_root_categories;
    public List<Category> lvCategories = new ArrayList<>();
    public List<Product> subCategories = new ArrayList<>();
    CategoryAdapter rootAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;

    ShoppingCartAdapter subAdapter;

    @BindView(R.id.lv_sub_categories) RecyclerView lv_sub_categories;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        try {
            db.setFirestoreSettings(settings);
        }catch (Exception ex){
            Log.e(TAG, "Firestore has already started", ex);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        //if (!pref.contains(getString(R.string.visite))) {
        // TODO : Open the tuto page
        //}else

        Fresco.initialize(this);
        /**
         * First run, force manual sync
         */
        /*if (!pref.contains(getString(R.string.last_time_sync))) {
            mAccount = CreateSyncAccount(this);
        }*/
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareBodyText = "Your help message goes here";
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject/Title");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(intent, "Choose message method"));
            }
        });


        rootAdapter = new CategoryAdapter(this, lvCategories);
        subAdapter = new ShoppingCartAdapter(this, subCategories);

        lv_root_categories.setLayoutManager(new LinearLayoutManager(this));
        lv_root_categories.setHasFixedSize(true);
        lv_root_categories.setAdapter(rootAdapter);

        lv_sub_categories.setLayoutManager(new LinearLayoutManager(this));
        lv_sub_categories.setHasFixedSize(true);
        lv_sub_categories.setAdapter(subAdapter);


        ItemClickSupport.addTo(lv_root_categories).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Category cat = lvCategories.get(position);
                db.collection("products").whereEqualTo("category", cat.getName())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.w(TAG, e.getMessage());
                            return;
                        }
                        subCategories.clear();
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            Log.i(TAG, doc.getId() + "=>" + doc.getData());
                            Product p = doc.toObject(Product.class);
                            subCategories.add(p);
                        }
                        subAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }



    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //active.onActivityResult(requestCode, resultCode, data);
    }*/



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            finish();
        }
        db.collection("categories").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, e.getMessage());
                    return;
                }
                lvCategories.clear();
                for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                    Category cat = doc.toObject(Category.class);
                    Log.d(TAG, doc.getId() + "=>" + doc.getData());
                    lvCategories.add(cat);
                }
                rootAdapter.notifyDataSetChanged();
            }
        });
        db.collection("products").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, e.getMessage());
                    return;
                }
                subCategories.clear();
                for(DocumentSnapshot doc : queryDocumentSnapshots){
                    Log.i(TAG, doc.getId() + "=>" + doc.getData());
                    Product p = doc.toObject(Product.class);
                    subCategories.add(p);
                }
                subAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        menuItem.setIcon(Converter.convertLayoutToImage(MainActivity.this,
                cart_count,
                R.drawable.ic_shopping_cart_white_24dp));
        MenuItem menuItem2 = menu.findItem(R.id.notification_action);
        menuItem2.setIcon(Converter.convertLayoutToImage(MainActivity.this,
                2,R.drawable.ic_notifications_white_24dp));
        return true;
    }
}
