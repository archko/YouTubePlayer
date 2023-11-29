package com.arch.youtube.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.arch.youtube.R;
import com.arch.youtube.common.ApiKey;
import com.arch.youtube.common.YoutubeApiHolder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;

public class YouTubeActivity extends AppCompatActivity {
    private static final String[] YOUTUBE_PLAYLISTS = {
            "PLWz5rJ2EKKc_Tt7q77qwyKRgytF1RzRx8",
            "PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE",
            "PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0",
            "PLWz5rJ2EKKc-lJo_RGGXL2Psr8vVCTWjM",
            "PLWz5rJ2EKKc9ofd2f-_-xmUi07wIGZa1c",
            "PLWz5rJ2EKKc-riD21lnOjVYBqSkNII3_k"
    };
    private static final String[] YOUTUBE_CHANNELS = {
            "UC_x5XG1OV2P6uZZ5FSM9Ttw",
            "UC3x4rHaszl7OIyEYC_J6aPA"
    };
    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity);

        if (ApiKey.YOUTUBE_API_KEY.startsWith("YOUR_API_KEY")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Edit ApiKey.java and replace \"YOUR_API_KEY\" with your Applications Browser API Key")
                    .setTitle("Missing API Key")
                    .setNeutralButton("Ok, I got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (savedInstanceState == null) {
            mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            YoutubeApiHolder.holder.mYoutubeApi = mYoutubeDataApi;

            /*getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, YouTubeRecyclerViewFragment.newInstance(mYoutubeDataApi, YOUTUBE_PLAYLISTS))
                    .commit();*/
        }
        test();
    }

    private void test() {
        /*new YouTubeChannelVideoTask(mYoutubeDataApi, YOUTUBE_CHANNELS[0]) {

            @Override
            protected void onPostExecute(List<Channel> channels) {
                super.onPostExecute(channels);
                System.out.println(channels.get(0));
                String[] playlistIds = new String[]{channels.get(0).getContentDetails().getRelatedPlaylists().getUploads()};
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, YouTubePlaylistItemsFragment.newInstance(
                                mYoutubeDataApi, playlistIds))
                        .commit();
            }
        }.execute();*/

        findViewById(R.id.test_playlist_by_channel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmptyFragmentActivity.startPlaylists(
                        YouTubePlaylistFragment.class.getName(),
                        "UC_x5XG1OV2P6uZZ5FSM9Ttw",
                        YouTubeActivity.this);
            }
        });
        findViewById(R.id.test_channel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OauthActivity.start(YouTubeActivity.this);
            }
        });
    }
}
