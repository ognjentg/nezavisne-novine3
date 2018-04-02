/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telegroup.testapp.storage.Category;
import com.telegroup.testapp.storage.News;

public class MainFragment extends BrowseFragment {

    private ArrayList<Category> categories;
    private static String APICall = "http://dtp.nezavisne.com/app/v2/vijesti/{id}";
    private HashMap<Integer, ArrayList<News>> vijesti = new HashMap<Integer, ArrayList<News>>();

    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;

    private boolean createdNews = false;
    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;
    private ArrayObjectAdapter rowsAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();

        prepareCategories();

        loadHeaders();

        loadRows();

        while(!createdNews){
            createNews(1, 15);
        }

        loadRows();

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadHeaders(){
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();
        int i;
        for(i = 0; i< categories.size(); i++){
            HeaderItem header = new HeaderItem(i, categories.get(i).getNaziv());
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }
        setAdapter(rowsAdapter);
    }

    private void loadRows() {
        // List<Movie> list = MovieList.setupMovies();

        CardPresenter cardPresenter = new CardPresenter();
        int i;
        for (i = 0; i < categories.size(); i++) {
//            if (i != 0) {
//                Collections.shuffle(list);
//            }
            //ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter)((ListRow)rowsAdapter.get(i)).getAdapter();
//            for (int j = 0; j < NUM_COLS; j++) {
//                listRowAdapter.add(list.get(j % 5));
//            }
            try{
                for(int j = 0; j < vijesti.get(categories.get(i).getMeniId()).size(); j++){
                    listRowAdapter.add(vijesti.get(categories.get(i).getMeniId()).get(j));
                }
            }catch(Exception ex){
                return;
            }
        }

        // HeaderItem gridHeader = new HeaderItem(i, "");
//
//        GridItemPresenter mGridPresenter = new GridItemPresenter();
//        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
//        gridRowAdapter.add(getResources().getString(R.string.grid_view));
//        gridRowAdapter.add(getString(R.string.error_fragment));
//        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
//        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(rowsAdapter);
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(getActivity(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(
        R.drawable.nezavisne_main_fragment));
        //setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title

        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));
        // set search icon color
        //setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));
    }

    private void prepareCategories() {
        categories=new ArrayList<>();

        try {
            ReadJsonFromUrl readJsonFromUrl =new ReadJsonFromUrl();
            String request = "http://dtp.nezavisne.com/app/meni";
            URL url = new URL(request);
            try{
                readJsonFromUrl.execute(url);
            }catch (Exception ex){
                ex.printStackTrace();
                return;
            }

            String jsonString = readJsonFromUrl.get();
            JsonElement element = new JsonParser().parse(jsonString);
            JsonArray nizPocetni = element.getAsJsonArray();

            for (int i = 0; i < nizPocetni.size(); i++) {
                JsonObject wObject = nizPocetni.get(i).getAsJsonObject();
                int meniId = Integer.valueOf(wObject.getAsJsonPrimitive("meniID").getAsString());
                String naziv = wObject.getAsJsonPrimitive("Naziv").getAsString();
                String boja = wObject.getAsJsonPrimitive("Boja").getAsString();
                categories.add(new Category(i, meniId, naziv, boja));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEventListeners() {
        /*setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });*/

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        resource.setColorFilter(0xFF7F7F7F, PorterDuff.Mode.MULTIPLY);
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            /*if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.error_fragment))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                }
            }*/
            if(item instanceof News){
                News vijest = (News) item;
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("vijest", vijest);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                //startActivity(intent, bundle);
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            /*if (item instanceof Movie) {
                mBackgroundUri = ((Movie) item).getBackgroundImageUrl();
                startBackgroundTimer();
            }*/
            if(item instanceof News){
                String preparedUri = ((News)item).getSlikaURL().get(0).replaceAll("/[0-9]*x[0-9]*/", "/750x450/");
                mBackgroundUri = preparedUri;
                ((News)item).getSlikaURL().set(0, mBackgroundUri);
                startBackgroundTimer();
            }
            //getView().setBackgroundColor(Color.parseColor(categories.get((int)row.getId()).getBoja()));
            setBrandColor(Color.parseColor(categories.get((int)row.getId()).getBoja()));

        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
    }


    public HashMap<Integer, ArrayList<News>> createNews(int pocetak, int kraj) {
        Integer kategorija = 0;
        System.out.println("CATEGORIES SIZE " + categories.size());
        for (int johnCleese = 0; johnCleese < categories.size() ; johnCleese++) {
            Category categoryIter = categories.get(johnCleese);
            String jsonString = null;
            try{
                String request = "https://dtp.nezavisne.com/app/rubrika/" + categoryIter.getMeniId() + "/+"+pocetak+"/"+kraj;
                URL url = new URL(request);
                ReadJsonFromUrl readJsonFromUrl = new ReadJsonFromUrl();
                readJsonFromUrl.execute(url);

                jsonString = readJsonFromUrl.get();
            }catch(Exception ex){
                ex.printStackTrace();
                johnCleese--;
                continue;
            }

            if(jsonString == null){
                johnCleese--;
                continue;
            }
            JsonElement element = new JsonParser().parse(jsonString);
            JsonArray nizPocetni = element.getAsJsonArray();
            ArrayList<News> news1 = new ArrayList<News>();
            for (int i = 0; i < nizPocetni.size(); i++) {
                News vijest = new News();
                JsonObject object = nizPocetni.get(i).getAsJsonObject();
                int vijestID = object.getAsJsonPrimitive("vijestID").getAsInt();
                String naslov = object.getAsJsonPrimitive("Naslov").getAsString();
                String lid = object.getAsJsonPrimitive("Lid").getAsString();
                String urlSlika = object.getAsJsonPrimitive("Slika").getAsString();
                kategorija = object.getAsJsonPrimitive("meniRoditelj").getAsInt();
                vijest.setMeniID(categoryIter.getMeniId());
                vijest.setColor(categoryIter.getBoja());
                vijest.setVijestID(vijestID);
                vijest.setNaslov(naslov);
                vijest.setLid(lid);
                vijest.getSlikaURL().add(urlSlika);
                news1.add(vijest);
            }
            System.out.println("DODAVANJE ****************");
            vijesti.put(kategorija, news1);
        }
        createdNews = true;
        return vijesti;
    }

    class ReadJsonFromUrl extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... urls) {
            try {
                URLConnection service = urls[0].openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(service.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String responseLine = "";
                while ((responseLine = reader.readLine()) != null) {
                    stringBuilder.append(responseLine);
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
