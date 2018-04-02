package com.telegroup.testapp.storage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Aleksina Matić
 */
public class News implements Serializable {

    private int vijestID;
    private String naslov;
    private String autor;
    private String datum;
    private String lid;
    private String tijelo;
    private int meniID;
    private String meniNaziv;
    private int meniRoditelj;
    private String meniRoditeljNaziv;
    private String meniRoditeljBoja;
    private String url;
    private String color;
    private ArrayList<String> slikeURL=new ArrayList<String>();

    public String getColor(){
        return color;
    }

    public void setColor(String color){
        this.color = color;
    }

    public int getVijestID() {
        return vijestID;
    }

    public void setVijestID(int vijestID) {
        this.vijestID = vijestID;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getTijelo() {
        return tijelo;
    }

    public void setTijelo(String tijelo) {
        this.tijelo = tijelo;
    }

    public int getMeniID() {
        return meniID;
    }

    public void setMeniID(int meniID) {
        this.meniID = meniID;
    }

    public String getMeniNaziv() {
        return meniNaziv;
    }

    public void setMeniNaziv(String meniNaziv) {
        this.meniNaziv = meniNaziv;
    }

    public int getMeniRoditelj() {
        return meniRoditelj;
    }

    public void setMeniRoditelj(int meniRoditelj) {
        this.meniRoditelj = meniRoditelj;
    }

    public String getMeniRoditeljNaziv() {
        return meniRoditeljNaziv;
    }

    public void setMeniRoditeljNaziv(String meniRoditeljNaziv) {
        this.meniRoditeljNaziv = meniRoditeljNaziv;
    }

    public String getMeniRoditeljBoja() {
        return meniRoditeljBoja;
    }

    public void setMeniRoditeljBoja(String meniRoditeljBoja) {
        this.meniRoditeljBoja = meniRoditeljBoja;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getSlikaURL() {
        return slikeURL;
    }

    public void setSlikaURL(ArrayList<String> slikeURL) {
        this.slikeURL = slikeURL;
    }
    public String toString(){


        return "Vijest ID: "+vijestID+" Naslov: "+naslov+" Autor:"+autor+" Datum: "+datum+" LID: "+lid+" Tijelo: "+tijelo;

    }
}
