/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.telegroup.testapp;

import android.os.AsyncTask;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telegroup.testapp.custompresenter.AbstractDetailsDescriptionPresenter;
import com.telegroup.testapp.storage.News;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private static String APICall = "http://dtp.nezavisne.com/app/v2/vijesti/{id}";

    public static News getNewsById(int id) {
        try {
            System.out.println("GETTING NEWS BY ID " + id);
            News vijest = new News();
            String request = APICall.replace("{id}", Integer.toString(id));
            URL url = new URL(request);

            ReadJsonFromUrl readJsonFromUrl = new ReadJsonFromUrl();
            readJsonFromUrl.execute(url);
            String jsonString = readJsonFromUrl.get();
            JsonElement element = new JsonParser().parse(jsonString);
            JsonObject object = element.getAsJsonObject();
            int vijestID = Integer.parseInt(object.getAsJsonPrimitive("vijestID").getAsString());
            String naslov = object.getAsJsonPrimitive("Naslov").getAsString();
            String autor = object.getAsJsonPrimitive("Autor").getAsString();
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String datum = object.getAsJsonPrimitive("Datum").getAsString();
            String lid = object.getAsJsonPrimitive("Lid").getAsString();
            String tijelo = object.getAsJsonPrimitive("Tjelo").getAsString();
            System.out.println(tijelo);
            int meniID = Integer.parseInt(object.getAsJsonPrimitive("meniID").getAsString());
            String meniNaziv = object.getAsJsonPrimitive("meniNaziv").getAsString();
            int meniRoditelj = Integer.parseInt(object.getAsJsonPrimitive("meniRoditelj").getAsString());
            String meniRoditeljNaziv = object.getAsJsonPrimitive("meniRoditeljNaziv").getAsString();
            String meniRoditeljBoja = object.getAsJsonPrimitive("meniRoditeljBoja").getAsString();
            String url1 = object.getAsJsonPrimitive("url").getAsString();
            String urlTwitter = object.getAsJsonPrimitive("urlTwitter").getAsString();
            JsonElement elementKategorije = object.get("Slika");
            JsonArray nizKategorije = elementKategorije.getAsJsonArray();
            for (int i = 0; i < nizKategorije.size(); i++) {
                vijest.getSlikaURL().add(nizKategorije.get(i).getAsJsonObject().getAsJsonPrimitive("slikaURL").getAsString());
            }
            vijest.setAutor(autor);
            vijest.setDatum(datum);
            vijest.setLid(lid);
            vijest.setMeniID(meniID);
            vijest.setMeniNaziv(meniNaziv);
            vijest.setMeniRoditelj(meniRoditelj);
            vijest.setMeniRoditeljBoja(meniRoditeljBoja);
            vijest.setMeniRoditeljNaziv(meniRoditeljNaziv);
            vijest.setNaslov(naslov);
            vijest.setTijelo(tijelo);
            vijest.setUrl(url1);
            vijest.setVijestID(vijestID);
            System.out.println("END OF GETBYID!");
            return vijest;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
//        Movie movie = (Movie) item;
//
//        if (movie != null) {
//            viewHolder.getTitle().setText(movie.getTitle());
//            viewHolder.getSubtitle().setText(movie.getStudio());
//            viewHolder.getBody().setText(movie.getDescription());
//        }
        News vijest = (News) item;
        if(vijest != null){
            viewHolder.getBody().setVerticalScrollBarEnabled(true);
            viewHolder.getBody().setMovementMethod(new ScrollingMovementMethod());
            viewHolder.getTitle().setText(vijest.getNaslov());
            News fullVijest = getNewsById(vijest.getVijestID());
            viewHolder.getSubtitle().setText("Autor: " + fullVijest.getAutor() +
                    "   " + "Datum: " + fullVijest.getDatum() + System.getProperty("line.separator") + fullVijest.getLid());
//            System.out.println("******************");
//            System.out.println(vijest.getLid());
            /*fullVijest.setTijelo(fullVijest.getTijelo().replaceAll("<p>", "")
                    .replaceAll("</p>", System.getProperty("line.separator")).replaceAll("&nbsp;", ""));*/
            //fullVijest.setTijelo(Html.fromHtml(fullVijest.getTijelo(), Html.FROM_HTML_MODE_LEGACY).toString());
            String parsedOnce = (Html.fromHtml(fullVijest.getTijelo())).toString();
            System.out.println("********************");
            System.out.println(parsedOnce);
            viewHolder.getBody().setText(parsedOnce.replaceAll("<.*>.*</.*>", "").replaceAll("￼", ""));
        }
    }
}

class ReadJsonFromUrl extends AsyncTask<URL, Integer, String> {

    @Override
    protected String doInBackground(URL... urls) {
        boolean done = false;
        while(!done){
            try {
                URLConnection service = urls[0].openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(service.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String responseLine = "";
                while ((responseLine = reader.readLine()) != null) {
                    stringBuilder.append(responseLine);
                }
                done = true;
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}