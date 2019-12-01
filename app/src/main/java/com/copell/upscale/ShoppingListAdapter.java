package com.copell.upscale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.copell.upscale.interfaces.AddOrRemoveCallbacks;
import com.copell.upscale.model.Product;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {
    private Context mContext;
    private List<Product> mCartItems;

    public ShoppingListAdapter(Context context, List<Product> cartItems) {
        this.mContext = context;
        this.mCartItems = cartItems;
    }

    @Override
    public ShoppingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int p1) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.cart_list_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public int getItemCount() {
        return mCartItems.size();
    }

    @Override
    public void onBindViewHolder(final ShoppingListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.bindItem(mCartItems.get(position));
        viewHolder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasBeenClicked(viewHolder, position);
            }
        });
        viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasBeenClicked(viewHolder, position);
            }
        });
    }
    private void hasBeenClicked(ShoppingListAdapter.ViewHolder viewHolder, int position){
        if(!mCartItems.get(position).isAddedTocart()) {
            mCartItems.get(position).setAddedTocart(true);
            viewHolder.addToCart.setVisibility(View.GONE);
            viewHolder.removeItem.setVisibility(View.VISIBLE);
            if(mContext instanceof MainActivity) {
                ((AddOrRemoveCallbacks)mContext).onAddProduct(mCartItems.get(position));
            }
        } else{
            mCartItems.get(position).setAddedTocart(false);
            //viewHolder.addRemove.setText("Add");
            viewHolder.addToCart.setVisibility(View.VISIBLE);
            viewHolder.removeItem.setVisibility(View.GONE);
            ((AddOrRemoveCallbacks)mContext).onRemoveProduct(mCartItems.get(position));
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView product_name;
        TextView product_price;
        ImageButton removeItem;
        ImageButton addToCart;
        ImageView product_image;


        public ViewHolder(View view) {
            super(view);
            this.itemView = view;
            product_name = itemView.findViewById(R.id.product_name);
            product_price = itemView.findViewById(R.id.product_price);
            removeItem = itemView.findViewById(R.id.removeItem);
            addToCart = itemView.findViewById(R.id.addToCart);
            product_image = itemView.findViewById(R.id.product_image);
        }

        public void bindItem(Product cartItem) {

            Picasso.with(mContext).load(cartItem.getImageURL())
                    .fit()
                    .into(product_image);
            product_name.setText(cartItem.getName());
            product_price.setText(String.format("$%s", cartItem.getPrice()));
            //if(cartItem.isAddedTocart()){
                addToCart.setVisibility(View.VISIBLE);
                removeItem.setVisibility(View.GONE);
            //}
        }
    }
}

