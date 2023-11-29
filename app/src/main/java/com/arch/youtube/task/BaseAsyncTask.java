package com.arch.youtube.task;

import android.os.AsyncTask;

import com.arch.youtube.common.YoutubeApiHolder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected YouTube mService;

    public BaseAsyncTask() {
        mService = YoutubeApiHolder.holder.mYoutube;
    }

    public BaseAsyncTask(YouTube api) {
        mService = api;
    }

    public BaseAsyncTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("YouTube Data API Android")
                .build();
    }

}
