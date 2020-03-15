package com.beatbac.model;

public class Book {

    private String name;
    private String section;
    private int img;
    private String url;
    private String file;

    public Book(String name, String section, int img, String url, String file) {
        this.name = name;
        this.section = section;
        this.img = img;
        this.url = url;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }

    public int getImg() {
        return img;
    }

    public String getUrl() {
        return url;
    }

    public String getFile() {
        return file;
    }

}
