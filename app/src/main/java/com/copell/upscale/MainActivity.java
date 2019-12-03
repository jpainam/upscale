package com.copell.upscale;

import android.accounts.Account;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;


import com.copell.upscale.interfaces.AddOrRemoveCallbacks;
import com.copell.upscale.model.Category;
import com.copell.upscale.model.Product;
import com.copell.upscale.utils.Converter;
import com.copell.upscale.utils.IntentIntegrator;
import com.copell.upscale.utils.ItemClickSupport;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements
        DiscountFragment.OnFragmentInteractionListener,
        InventoryFragment.OnFragmentInteractionListener, AddOrRemoveCallbacks {

    private static final int CAMERA_SCAN_REQUEST = 1000;
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
    FirebaseUser user;
    MainActivity  mActivity;
    @BindView(R.id.toolbar) Toolbar toolbar;

    ShoppingListAdapter subAdapter;

    @BindView(R.id.lv_sub_categories) RecyclerView lv_sub_categories;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener = null;

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
        mActivity = this;

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
                startActivityForResult(new Intent(MainActivity.this, ScanBarcodeActivity.class), CAMERA_SCAN_REQUEST);
                //IntentIntegrator scanIntegrator = new IntentIntegrator(mActivity);
                //scanIntegrator.initiateScan();
            }
        });


        rootAdapter = new CategoryAdapter(this, lvCategories);
        subAdapter = new ShoppingListAdapter(this, subCategories);

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




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Result ok != RESULT_OK in onActivityResult");
            return;
        }
        if(requestCode == CAMERA_SCAN_REQUEST){
            String result = data.getStringExtra("value");
            //Log.d(TAG , "un peu for " + result);
            if(!result.isEmpty()){
                String[] tab = result.split("#");
                String id = tab[0];
                String name = "";
                int price = 0;

                db.collection("products").document(id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Product p = task.getResult().toObject(Product.class);
                                onAddProduct(p);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onActivityResult:", e);
                    }
                });
                /*if (tab.length > 1) {
                    id = tab[0];
                    name = tab[1];
                    price = Integer.valueOf(tab[2]);
                    Product p = new Product(id, name, price);
                    onAddProduct(p);
                }*/
            }
        }

    }




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
                    p.setId(doc.getId());
                    subCategories.add(p);
                }
                subAdapter.notifyDataSetChanged();
            }
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
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
        //MenuItem menuItem2 = menu.findItem(R.id.action_logout);
        //menuItem2.setIcon(Converter.convertLayoutToImage(MainActivity.this,
        //        2,R.drawable.ic_notifications_white_24dp));
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener(){
                @Override
                public boolean onQueryTextChange(String newText){
                    Log.e("onQueryTextSubmit", newText);
                    filterProduct(newText);
                    return true;

                }

                @Override
                public boolean onQueryTextSubmit(String query){
                    Log.e("onQueryTextSubmit", query);
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return true;
    }

    private void filterProduct(String newText) {
        subAdapter.searchProduct(newText);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.cart_action:
                intent = new Intent(MainActivity.this, ShoppingCart.class);
                startActivity(intent);
                return true;
            case  R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAddProduct(Product p) {
        cart_count++;
        /*if(selectedProducts.toString().indexOf(p.getId()) == -1) {
            selectedProducts.append(p.getId()).append("#");
        }*/

        Map<String, Object> map = new HashMap<>();
        map.put("idproduct", p.getId());
        map.put("iduser",  FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("price", p.getPrice());
        map.put("image", p.getImageURL());
        db.collection("purchases").document(String.format("%s%s",
                p.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid())).set(map);
        invalidateOptionsMenu();
        Snackbar.make((CoordinatorLayout)findViewById(R.id.rootView), "Added to cart !!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();


    }

    @Override
    public void onRemoveProduct(Product p) {
        cart_count--;
        invalidateOptionsMenu();
        /*if(selectedProducts.indexOf(p.getId()) != -1){
            selectedProducts.delete(selectedProducts.indexOf(p.getId()),
                    selectedProducts.indexOf(p.getId()) + p.getId().length());
            Log.d(TAG, "Removed " + selectedProducts.toString());
        }*/
        db.collection("purchases").document(String.format("%s%s",
                p.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid())).delete();
        Snackbar.make((CoordinatorLayout)findViewById(R.id.rootView), "Removed from cart !!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }
}
