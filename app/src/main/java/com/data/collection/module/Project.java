package com.data.collection.module;

import java.util.List;

public class Project {
    private String id;

    private String name;

    private List<Types> types ;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setTypes(List<Types> types){
        this.types = types;
    }
    public List<Types> getTypes(){
        return this.types;
    }
}
