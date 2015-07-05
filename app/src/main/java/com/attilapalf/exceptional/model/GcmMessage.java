package com.attilapalf.exceptional.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 212461305 on 2015.06.30..
 */
public class GcmMessage {

    private Data data;
    private List<String> registration_ids;

    public GcmMessage() {

    }

    public GcmMessage(String regId) {
        registration_ids = new ArrayList<>();
        registration_ids.add(regId);

        data = new Data("lol data", "very", 6);
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<String> getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }


    public static class Data {
        private String name;
        private String coolness;
        private int level;

        public Data () {

        }

        public Data(String name, String coolness, int level) {
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

}



