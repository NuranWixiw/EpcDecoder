package com.essen.epcdecoder.models;

public class Section {

    private String  id;
    private String displayValue;
    public  Section(String id ,String value){
        this.setId(id);
        this.setDisplayValue(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
}
