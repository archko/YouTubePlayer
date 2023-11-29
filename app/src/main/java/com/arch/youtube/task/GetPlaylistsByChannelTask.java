package com.arch.youtube.task;

import android.os.AsyncTask;

import com.arch.youtube.common.ApiKey;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.io.IOException;
import java.util.Collections;

public class GetPlaylistsByChannelTask extends BaseAsyncTask<String, Void, PlaylistListResponse> {
    //https://developers.google.com/youtube/v3/docs/playlists/list?hl=zh-cn&apix_params=%7B"part"%3A%5B"snippet%2CcontentDetails"%5D%2C"channelId"%3A"UC_x5XG1OV2P6uZZ5FSM9Ttw"%2C"maxResults"%3A25%7D&apix=true
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,contentDetails,snippet(title,thumbnails,publishedAt,description))";

    private String channelId = null;

    public GetPlaylistsByChannelTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();
    }

    public GetPlaylistsByChannelTask(YouTube mService, String channelId) {
        this.mService = mService;
        this.channelId = channelId;
    }

    @Override
    protected PlaylistListResponse doInBackground(String... params) {
        //String channelId = params[0];

        PlaylistListResponse playlistResponse;
        try {
            playlistResponse = mService.playlists()
                    .list(Collections.singletonList(YOUTUBE_PLAYLIST_PART))
                    //.setId(Arrays.asList(playlistIds))
                    .setChannelId(channelId)   //设置频道id
                    .setMaxResults(10L)
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return playlistResponse;
    }
}
