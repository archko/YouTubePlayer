package com.arch.youtube.task;

import android.os.AsyncTask;

import com.arch.youtube.common.ApiKey;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 这个逻辑很奇怪,只有频道id时,频道内容是不知道的,所以需要根据id获取频道列表,然后得到一个列表
 * 取列表中的第一项,其中的.getContentDetails().getRelatedPlaylists().getUploads()就是播放列表.
 * playlists()与playlistItems()的区别,前者是播放列表,后者是播放列表中的视频列表,是一对比的关系
 * 用这个id可以使用mYouTubeDataApi.playlistItems()来获取相关的视频列表,分页参数要带上,否则不会获取所有的.
 */
public class YouTubeChannelVideoTask extends AsyncTask<Void, Void, List<Channel>> {
    private YouTube mService = null;
    private String channelId = null;
    private Exception mLastError = null;

    public YouTubeChannelVideoTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();
    }

    public YouTubeChannelVideoTask(YouTube mService, String channelId) {
        this.mService = mService;
        this.channelId = channelId;
    }

    @Override
    protected List<Channel> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            System.out.println(e);
            cancel(true);
            return null;
        }
    }

    private List<Channel> getDataFromApi() {
        ChannelListResponse channelResult = null;
        try {
            channelResult = mService.channels().list(Collections.singletonList("snippet,contentDetails,statistics"))
                    //.setMine(true)
                    .setId(Collections.singletonList(channelId))
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    .setFields("items(contentDetails,snippet),nextPageToken,pageInfo")
                    .execute();

            System.out.println("channelResult:" + channelResult);

            List<Channel> channelsList = channelResult.getItems();
            // Define a list to store items in the list of uploaded videos.
            List<PlaylistItem> playlistItemList = new ArrayList<>();

            if (channelsList != null) {
                /*System.out.println(channelsList.get(0));

                //youtubeUser.addUpload(channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads());

                // The user's default channel is the first item in the list.
                // Extract the playlist ID for the channel's videos from the
                // API response.
                String uploadPlaylistId =
                        channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();


                // Retrieve the playlist of the channel's uploaded videos.
                YouTube.PlaylistItems.List playlistItemRequest =
                        mService.playlistItems().list(Collections.singletonList("id,contentDetails,snippet"));
                playlistItemRequest.setPlaylistId(uploadPlaylistId);
                playlistItemRequest.setKey(ApiKey.YOUTUBE_API_KEY);

                // Only retrieve data used in this application, thereby making
                // the application more efficient. See:
                // https://developers.google.com/youtube/v3/getting-started#partial
                playlistItemRequest.setFields("items(contentDetails/videoId,snippet),nextPageToken,pageInfo");

                String nextToken = "";

                // Call the API one or more times to retrieve all items in the
                // list. As long as the API response returns a nextPageToken,
                // there are still more items to retrieve.
                //do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                //} while (nextToken != null);

                // Prints information about the results.
                processInfo(playlistItemList);*/
            }
            return channelsList;
        } catch (UserRecoverableAuthIOException e) {
            System.out.println("UserRecoverableAuthIOException");
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
            /*if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }*/
    }
}

