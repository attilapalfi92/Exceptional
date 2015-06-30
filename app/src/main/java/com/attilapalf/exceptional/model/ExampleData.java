package com.attilapalf.exceptional.model;

/**
 * Created by 212461305 on 2015.06.30..
 */
public class ExampleData {
    private String name;
    private String coolness;
    private int level;

    public ExampleData() {
    }

    public ExampleData(String name, String coolness, int level) {
        this.name = name;
        this.coolness = coolness;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoolness() {
        return coolness;
    }

    public void setCoolness(String coolness) {
        this.coolness = coolness;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
