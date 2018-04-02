package com.telegroup.testapp.storage;

/**
 * Created by Nemanja Đokić on 18/03/27.
 */

public class Category {
    private int rowId;
    private int meniId;
    private String naziv;
    private String boja;

    public Category(int rowId, int meniId, String naziv, String boja){
        this.rowId = rowId;
        this.meniId = meniId;
        this.naziv = naziv;
        this.boja = boja;
    }

    public int getRowId(){
        return rowId;
    }

    public int getMeniId(){
        return meniId;
    }

    public String getNaziv(){
        return naziv;
    }

    public String getBoja(){
        return boja;
    }
}
