package com.copell.upscale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;

import com.copell.upscale.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart extends AppCompatActivity {

    @BindView(R.id.shopping_cart_recyclerView)
    RecyclerView shopping_cart_recyclerView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ShoppingCartAdapter adapter;
    List<Product> productList = new ArrayList<>();

    String cartProducts = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        ButterKnife.bind(this);
        cartProducts = getIntent().getStringExtra("cartproducts");

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(cartProducts == null || cartProducts.length() == 0){
            return;
        }

        db.collection("products");
    }
}
