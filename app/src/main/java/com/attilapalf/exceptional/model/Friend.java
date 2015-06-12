package com.attilapalf.exceptional.model;

import com.google.gson.Gson;

import java.util.Comparator;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend {
    private long id;
    private String name;
    private String imageUrl;

    private static Gson gson = new Gson();

    public static class NameComparator implements Comparator<Friend> {
        @Override
        public int compare(Friend lhs, Friend rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public static Friend fromString(String friendJson) {
        return gson.fromJson(friendJson, Friend.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return ((Friend)o).id == id;
    }

    public Friend() {
    }

    public Friend(long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
