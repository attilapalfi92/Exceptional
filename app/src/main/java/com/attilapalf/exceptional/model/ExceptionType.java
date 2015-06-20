package com.attilapalf.exceptional.model;

import java.util.Comparator;

/**
 * Created by Attila on 2015-06-14.
 */
public class ExceptionType {
    private int typeId;
    private String shortName;
    private String prefix;
    private String description;

    public static class IdComparator implements Comparator<ExceptionType> {
        @Override
        public int compare(ExceptionType lhs, ExceptionType rhs) {
            return lhs.getTypeId() < rhs.getTypeId() ? -1 :
                    (lhs.getTypeId() == rhs.getTypeId() ? 0 : 1);
        }
    }

    public static class ShortNameComparator implements Comparator<ExceptionType> {
        @Override
        public int compare(ExceptionType lhs, ExceptionType rhs) {
            return lhs.getShortName().compareTo(rhs.getShortName());
        }
    }

    public ExceptionType() {
    }

    public ExceptionType(int typeId, String shortName, String prefix, String description) {
        this.typeId = typeId;
        this.shortName = shortName;
        this.prefix = prefix;
        this.description = description;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
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
}
