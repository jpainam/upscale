package com.copell.upscale.model;


import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    public String description;
    public String id;
    public String name;
    public int price;
    public List<Photo> photos;
    public String imageURL;

    private boolean addedTocart = false;

    public Product(){

    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Product(String description, String id, String name, int price, List<Photo> photos) {
        this.description = description;
        this.id = id;
        this.name = name;
        this.price = price;
        this.photos = photos;
    }

    public boolean isAddedTocart() {
        return addedTocart;
    }

    public void setAddedTocart(boolean addedTocart) {
        this.addedTocart = addedTocart;
    }
}