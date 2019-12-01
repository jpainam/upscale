package com.copell.upscale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.copell.upscale.model.Product;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ShoppingCart extends AppCompatActivity {

    @BindView(R.id.shopping_cart_recyclerView)
    RecyclerView shopping_cart_recyclerView;

    @BindView(R.id.total_price)
    TextView total_price;

    @BindView(R.id.checkout)
    Button checkout;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ShoppingCartAdapter adapter;

    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        ButterKnife.bind(this);

        total_price = findViewById(R.id.total_price);

        adapter = new ShoppingCartAdapter(this, products);
        shopping_cart_recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        shopping_cart_recyclerView.setHasFixedSize(true);
        shopping_cart_recyclerView.setAdapter(adapter);

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make()
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        db.collection("purchases").whereEqualTo("iduser",
                FirebaseAuth.getInstance().getCurrentUser().getUid())
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(e != null){
                    Log.e("ShoppingCart", e.getMessage());
                    return;
                }
                int totalPrice = 0;
                products.clear();
                for(DocumentSnapshot doc : queryDocumentSnapshots){
                    Map<String, Object> map = doc.getData();
                    Product p = new Product();
                    if(map.get("idproduct") != null) {
                        p.setId(map.get("idproduct").toString());
                    }
                    p.setPrice(Integer.valueOf(map.get("price").toString()));
                    totalPrice += p.getPrice();
                    p.setImageURL(map.get("image").toString());
                    products.add(p);
                }
                if(total_price != null) {
                    total_price.setText(" $" + String.valueOf(totalPrice));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
