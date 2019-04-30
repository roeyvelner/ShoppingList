package com.shoppinglist.family.shoppinglist;

public class Item {


    private boolean isChecked;
    private String name;
    private  int id;

    public Item(){
        isChecked = false;
        name="";
        id = 1;//
    }
    public Item(Item item){
        this.isChecked = item.isChecked;
        this.name=item.name;
        this.id = id;
    }
    public Item(boolean isChecked,String name,int id){
        this.isChecked = isChecked;
        this.name=name;
        this.id = id;
    }


    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
