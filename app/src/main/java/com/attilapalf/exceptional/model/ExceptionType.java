package com.attilapalf.exceptional.model;

import com.google.gson.Gson;

import java.util.Comparator;

/**
 * Created by Attila on 2015-06-14.
 */
public class ExceptionType {
    private static Gson gson = new Gson();

    private int id;
    private int version;
    private int voteCount;
    private String shortName;
    private String prefix;
    private String description;
    private String type;
    private Submitter submitter;

    public static class VoteComparator implements Comparator<ExceptionType> {
        @Override
        public int compare(ExceptionType lhs, ExceptionType rhs) {
            return Integer.compare(lhs.getVoteCount(), rhs.getVoteCount());
        }
    }

    public static class IdComparator implements Comparator<ExceptionType> {
        @Override
        public int compare(ExceptionType lhs, ExceptionType rhs) {
            return lhs.getId() < rhs.getId() ? -1 :
                    (lhs.getId() == rhs.getId() ? 0 : 1);
        }

    }
    public static class ShortNameComparator implements Comparator<ExceptionType> {
        @Override
        public int compare(ExceptionType lhs, ExceptionType rhs) {
            return lhs.getShortName().compareTo(rhs.getShortName());
        }

    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public static ExceptionType fromString(String json) {
        return gson.fromJson(json, ExceptionType.class);
    }

    public ExceptionType() {
    }

    public ExceptionType(int id, String shortName, String prefix, String description) {
        this.id = id;
        this.shortName = shortName;
        this.prefix = prefix;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Submitter getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Submitter submitter) {
        this.submitter = submitter;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public class Submitter {
        private String firstName;
        private String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
