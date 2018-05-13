package com.example.vitomisur.cnewsr;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.content.Loader;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView emptyTextViewState;
    private ProgressBar loadingBar;
    public TextView newsApiTextView;
    private boolean isConnected = false;
    private static final int NEWS_LOADER_ID = 1;
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String NEWS_URL = "https://content.guardianapis.com/search";
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView newsListView = findViewById(R.id.list);
        newsApiTextView = findViewById(R.id.news_api);
        // Check if the network connection is available
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "isConnected error", e);
        }
        loadingBar = findViewById(R.id.loading_spinner);
        emptyTextViewState = findViewById(R.id.empty_view);
        // empty view - if there is no news or while loading them
        newsListView.setEmptyView(emptyTextViewState);
        // set adapter for array list
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(newsAdapter);

        // get & set user settings and set listener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // set/change adapter by the user preferences
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(newsAdapter);
        loadingBar = findViewById(R.id.loading_spinner);
        emptyTextViewState = findViewById(R.id.empty_view);
        newsListView.setEmptyView(emptyTextViewState);
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = newsAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);
    }

    /**
     *
     * @param prefs
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // change the loader by user setting
        if (key.equals(getString(R.string.settings_star_rating_key)) ||
                key.equals(getString(R.string.settings_section_key)) ||
                key.equals(getString(R.string.settings_page_key))) {
            newsAdapter.clear();
            emptyTextViewState.setVisibility(View.GONE);
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);
            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        // sets saved user settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String page = sharedPrefs.getString(
                getString(R.string.settings_page_key),
                getString(R.string.settings_page_default)
        );

        String starRating = sharedPrefs.getString(
                getString(R.string.settings_star_rating_key),
                getString(R.string.settings_star_rating_default)
        );
        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );

        // creates a uri based on settings
        Uri baseUri = Uri.parse(NEWS_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("api-key", "8099c47e-3f26-4815-9c70-d30a7d939c6a");

        if (starRating != null && !starRating.equals("none") && section.trim().length() != 0 && !section.isEmpty()) {
            uriBuilder.appendQueryParameter("star-rating", starRating);
        } else if (!starRating.equals("none") && section.equals("newest")) {
            Toast.makeText(this, "Newest news may not have star rating yet",
                    Toast.LENGTH_LONG).show();
        }
        if (section != null && section.trim().length() != 0 && !section.isEmpty() && !section.equals("newest")) {
            uriBuilder.appendQueryParameter("section", section);
        }
        if (!(Integer.parseInt(page) <= 0)) {
            uriBuilder.appendQueryParameter("page", page);
        } else {
            Toast.makeText(this, "Wrong page number(should be positive)",
                    Toast.LENGTH_LONG).show();
        }
        Log.v("URL is:", uriBuilder.toString());
        return new NewsLoader(this, uriBuilder.toString());
    }


    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // fill in the activity with news if they exist and hide everything else
        loadingBar.setVisibility(GONE);
        newsApiTextView.setVisibility(GONE);
        newsAdapter.clear();
        if (news != null && !news.isEmpty()) {
            newsAdapter.addAll(news);
        } else if (!isConnected) {
            emptyTextViewState.setText(R.string.no_connection);
        } else {
            emptyTextViewState.setText(R.string.no_news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
