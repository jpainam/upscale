package com.copell.upscale.model;


import java.util.List;

public class Product {
    public String description;
    public int id;
    public String name;
    public int price;
    public List<Photo> photos;
    public String imageURL;

    public Product(){

    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Product(String description, int id, String name, int price, List<Photo> photos) {
        this.description = description;
        this.id = id;
        this.name = name;
        this.price = price;
        this.photos = photos;
    }
}