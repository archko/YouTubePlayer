package com.arch.youtube.common;

import android.content.Context;

import com.arch.youtube.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

/**
 * @author: archko 2023/11/27 :18:33
 */
public class YoutubeApiHolder {

    public YouTube mYoutube;

    public static YoutubeApiHolder holder = Factory.instance;

    private static class Factory {
        static YoutubeApiHolder instance = new YoutubeApiHolder();
    }

    public static void setApi(GoogleAccountCredential credential, Context context) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        holder.mYoutube = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(context.getResources().getString(R.string.app_name))
                .build();
    }
}