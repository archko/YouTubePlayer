package com.arch.youtube.ui;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.arch.youtube.R;
import com.arch.youtube.common.ApiKey;
import com.arch.youtube.model.PlaylistVideos;
import com.arch.youtube.task.GetPlaylistAsyncTask;
import com.arch.youtube.task.GetPlaylistTitlesAsyncTask;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * YouTubeRecyclerViewFragment contains a RecyclerView that shows a list of YouTube video cards.
 * <p/>
 */
public class YouTubeRecyclerViewFragment extends Fragment {
    // the fragment initialization parameter
    private static final String ARG_YOUTUBE_PLAYLIST_IDS = "YOUTUBE_PLAYLIST_IDS";
    private static final int SPINNER_ITEM_LAYOUT_ID = R.layout.simple_spinner_item;
    private static final int SPINNER_ITEM_DROPDOWN_LAYOUT_ID = R.layout.simple_spinner_dropdown_item;

    private String[] mPlaylistIds;
    private ArrayList<String> mPlaylistTitles;
    private RecyclerView mRecyclerView;
    private PlaylistVideos mPlaylistVideos;
    private RecyclerView.LayoutManager mLayoutManager;
    private Spinner mPlaylistSpinner;
    private PlaylistCardAdapter mPlaylistCardAdapter;
    private ProgressDialog mProgressDialog;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param youTubeDataApi
     * @param playlistIds    A String array of YouTube Playlist IDs
     * @return A new instance of fragment YouTubeRecyclerViewFragment.
     */
    public static YouTubeRecyclerViewFragment newInstance(String[] playlistIds) {
        YouTubeRecyclerViewFragment fragment = new YouTubeRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_YOUTUBE_PLAYLIST_IDS, playlistIds);
        fragment.setArguments(args);
        return fragment;
    }

    public YouTubeRecyclerViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mPlaylistIds = getArguments().getStringArray(ARG_YOUTUBE_PLAYLIST_IDS);
        }

        mProgressDialog = new ProgressDialog(getContext());

        // start fetching the playlist titles
        new GetPlaylistTitlesAsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mProgressDialog != null) {
                    mProgressDialog.show();
                }
            }

            @Override
            protected void onPostExecute(PlaylistListResponse playlistListResponse) {
                // if we didn't receive a response for the playlist titles, then there's nothing to update
                if (playlistListResponse == null)
                    return;

                mPlaylistTitles = new ArrayList();
                for (com.google.api.services.youtube.model.Playlist playlist : playlistListResponse.getItems()) {
                    mPlaylistTitles.add(playlist.getSnippet().getTitle());
                }
                // update the spinner adapter with the titles of the playlists
                ArrayAdapter<List<String>> spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mPlaylistTitles);
                spinnerAdapter.setDropDownViewResource(SPINNER_ITEM_DROPDOWN_LAYOUT_ID);
                mPlaylistSpinner.setAdapter(spinnerAdapter);
                mProgressDialog.hide();
            }
        }.execute(mPlaylistIds);

        //new MakeRequestTask(mYouTubeDataApi).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // set the Picasso debug indicator only for debug builds

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.youtube_recycler_view_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.youtube_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        Resources resources = getResources();
        if (resources.getBoolean(R.bool.isTablet)) {
            // use a staggered grid layout if we're on a large screen device
            mLayoutManager = new StaggeredGridLayoutManager(resources.getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL);
        } else {
            // use a linear layout on phone devices
            mLayoutManager = new LinearLayoutManager(getActivity());
        }

        mRecyclerView.setLayoutManager(mLayoutManager);

        mPlaylistSpinner = (Spinner) rootView.findViewById(R.id.youtube_playlist_spinner);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // if we have a playlist in our retained fragment, use it to populate the UI
        if (mPlaylistVideos != null) {
            // reload the UI with the existing playlist.  No need to fetch it again
            reloadUi(mPlaylistVideos, false);
        } else {
            // otherwise create an empty playlist using the first item in the playlist id's array
            mPlaylistVideos = new PlaylistVideos(mPlaylistIds[0]);
            // and reload the UI with the selected playlist and kick off fetching the playlist content
            reloadUi(mPlaylistVideos, true);
        }

        ArrayAdapter<List<String>> spinnerAdapter;
        // if we don't have the playlist titles yet
        if (mPlaylistTitles == null || mPlaylistTitles.isEmpty()) {
            // initialize the spinner with the playlist ID's so that there's something in the UI until the GetPlaylistTitlesAsyncTask finishes
            spinnerAdapter = new ArrayAdapter(getContext(), SPINNER_ITEM_LAYOUT_ID, Arrays.asList(mPlaylistIds));
        } else {
            // otherwise use the playlist titles for the spinner
            spinnerAdapter = new ArrayAdapter(getContext(), SPINNER_ITEM_LAYOUT_ID, mPlaylistTitles);
        }

        spinnerAdapter.setDropDownViewResource(SPINNER_ITEM_DROPDOWN_LAYOUT_ID);
        mPlaylistSpinner.setAdapter(spinnerAdapter);

        // set up the onItemSelectedListener for the spinner
        mPlaylistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // reload the UI with the playlist video list of the selected playlist
                mPlaylistVideos = new PlaylistVideos(mPlaylistIds[position]);
                reloadUi(mPlaylistVideos, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void reloadUi(final PlaylistVideos playlistVideos, boolean fetchPlaylist) {
        // initialize the cards adapter
        initCardAdapter(playlistVideos);

        if (fetchPlaylist) {
            // start fetching the selected playlistVideos contents
            new GetPlaylistAsyncTask() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (mProgressDialog != null) {
                        mProgressDialog.show();
                    }
                }

                @Override
                public void onPostExecute(Pair<String, List<Video>> result) {
                    handleGetPlaylistResult(playlistVideos, result);
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                }

            }.execute(playlistVideos.playlistId, playlistVideos.getNextPageToken());
        }
    }

    private void initCardAdapter(final PlaylistVideos playlistVideos) {
        // create the adapter with our playlistVideos and a callback to handle when we reached the last item
        mPlaylistCardAdapter = new PlaylistCardAdapter(playlistVideos, new LastItemReachedListener() {
            @Override
            public void onLastItem(int position, String nextPageToken) {
                new GetPlaylistAsyncTask() {
                    @Override
                    public void onPostExecute(Pair<String, List<Video>> result) {
                        handleGetPlaylistResult(playlistVideos, result);
                    }
                }.execute(playlistVideos.playlistId, playlistVideos.getNextPageToken());
            }
        });
        mRecyclerView.setAdapter(mPlaylistCardAdapter);
    }

    private void handleGetPlaylistResult(PlaylistVideos playlistVideos, Pair<String, List<Video>> result) {
        if (result == null) return;
        final int positionStart = playlistVideos.size();
        playlistVideos.setNextPageToken(result.first);
        playlistVideos.addAll(result.second);
        mPlaylistCardAdapter.notifyItemRangeInserted(positionStart, result.second.size());
    }


    /**
     * Interface used by the {@link PlaylistCardAdapter} to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        MakeRequestTask(YouTube mService) {
            this.mService = mService;
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                System.out.println(e);
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<>();
            ChannelListResponse result = mService.channels().list(Collections.singletonList("snippet,contentDetails,statistics"))
                    .setId(Collections.singletonList("UCXuqSBlHAE6Xw-yeJA0Tunw"))
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    //.setForUsername("GoogleDevelopers")
                    .execute();
            List<Channel> channels = result.getItems();
            if (channels != null) {
                Channel channel = channels.get(0);
                channelInfo.add("This channel's ID is " + channel.getId() + ". " +
                        "Its title is '" + channel.getSnippet().getTitle() + ", " +
                        "and it has " + channel.getStatistics().getViewCount() + " views.");
            }
            return channelInfo;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                System.out.println(String.format("=====%s", output));
            }
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
}
