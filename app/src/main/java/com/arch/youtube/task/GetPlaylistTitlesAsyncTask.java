package com.arch.youtube.task;

import android.os.AsyncTask;

import com.arch.youtube.common.ApiKey;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * This BaseAsyncTask will get the titles of all the playlists that are passed in as a parameter.
 */
public class GetPlaylistTitlesAsyncTask  extends BaseAsyncTask<String[], Void, PlaylistListResponse> {
    //see: https://developers.google.com/youtube/v3/docs/playlists/list
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title))";

    @Override
    protected PlaylistListResponse doInBackground(String[]... params) {

        final String[] playlistIds = params[0];

        PlaylistListResponse playlistListResponse;
        try {
            playlistListResponse = mService.playlists()
                .list(Collections.singletonList(YOUTUBE_PLAYLIST_PART))
                .setId(Arrays.asList(playlistIds))
                .setFields(YOUTUBE_PLAYLIST_FIELDS)
                .setKey(ApiKey.YOUTUBE_API_KEY)
                .execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
         
        return playlistListResponse;
    }
}
