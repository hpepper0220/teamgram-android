package org.telegram.ext.model;

public class MenuItem {

    private int id;
    private String title;
    private int iconResource;

    public MenuItem() {
    }

    public MenuItem(int id, String title, int iconResource) {
        this.id = id;
        this.title = title;
        this.iconResource = iconResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
}
