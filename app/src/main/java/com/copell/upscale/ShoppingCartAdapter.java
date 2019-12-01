package com.copell.upscale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.copell.upscale.model.CartItem;
import com.copell.upscale.model.Product;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {
    private Context mContext;
    private List<Product> mCartItems;

    public ShoppingCartAdapter(Context context, List<Product> cartItems) {
        this.mContext = context;
        this.mCartItems = cartItems;
    }

    @Override
    public ShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int p1) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.cart_list_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public int getItemCount() {
        return mCartItems.size();
    }

    @Override
    public void onBindViewHolder(ShoppingCartAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bindItem(mCartItems.get(position));
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView product_name;
        TextView product_price;
        TextView product_quantity;
        ImageView product_image;


        public ViewHolder(View view) {
            super(view);
            this.itemView = view;
            product_name = itemView.findViewById(R.id.product_name);
            product_price = itemView.findViewById(R.id.product_price);
            product_quantity = itemView.findViewById(R.id.product_quantity);
            product_image = itemView.findViewById(R.id.product_image);
        }

        public void bindItem(Product cartItem) {

            Picasso.with(mContext).load(cartItem.getImageURL())
                    .fit()
                    .into(product_image);
            product_name.setText(cartItem.getName());
            product_price.setText(String.format("$%s", cartItem.getPrice()));
            product_quantity.setText("");
        }
    }
}

