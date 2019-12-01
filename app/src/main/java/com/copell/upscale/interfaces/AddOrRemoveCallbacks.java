package com.copell.upscale.interfaces;

import com.copell.upscale.model.Product;

public interface AddOrRemoveCallbacks {

    public void onAddProduct(Product p);
    public void onRemoveProduct(Product p);
}
