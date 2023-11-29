package com.arch.youtube.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arch.youtube.R;
import com.arch.youtube.common.ApiKey;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author: archko 2023/11/29 :10:55
 */
public class OauthActivity extends Activity {

    public static void start(Context context) {
        Intent intent = new Intent(context, OauthActivity.class);
        context.startActivity(intent);
    }

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {
            YouTubeScopes.YOUTUBE_READONLY,
            YouTubeScopes.YOUTUBEPARTNER,
            YouTubeScopes.YOUTUBE_FORCE_SSL
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        Button mCallApiButton = findViewById(R.id.btnOauth);
        mCallApiButton.setOnClickListener(v -> {
            mOutputText.setText("");
            getResultsFromApi();
        });

        EditText etSearch = findViewById(R.id.etSearch);

        mCallApiButton = findViewById(R.id.btnSearch);
        mCallApiButton.setOnClickListener(v -> {
            performYoutubeSearch(etSearch.getEditableText().toString());
        });

        mCallApiButton = findViewById(R.id.btnWl);
        mCallApiButton.setOnClickListener(v -> {
            performYoutubeWl("C3-WnwzsaJA");
        });

        mCallApiButton = findViewById(R.id.btnSub);
        mCallApiButton.setOnClickListener(v -> {
            performYoutubeSub("UCEjOSbbaOfgnfRODEEMYlCw");
        });

        mOutputText = findViewById(R.id.output);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT + "\' button to test the API.");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        mCredential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        SharedPreferences settings =
                getPreferences(Context.MODE_PRIVATE);
        String accountName = settings.getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
        }
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    private void chooseAccount() {
        try {
            startActivityForResult(mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        } catch (Exception e) {
            Toast.makeText(this, "open google account error:" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                OauthActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<String>();
            ChannelListResponse result = mService.channels().list(Collections.singletonList("snippet,contentDetails,statistics"))
                    .setForUsername("GoogleDevelopers")
                    .execute();
            List<Channel> channels = result.getItems();
            if (channels != null) {
                Channel channel = channels.get(0);
                channelInfo.add("This channel's ID is " + channel.getId() + ". " +
                        "Its title is '" + channel.getSnippet().getTitle() + ", " +
                        "and it has " + channel.getStatistics().getViewCount() + " views." +
                        channel.getContentDetails().toPrettyString());
                if (null != channel.getContentDetails().getRelatedPlaylists()) {
                    String history = channel.getContentDetails().getRelatedPlaylists().getWatchHistory();
                    channelInfo.add(" history:" + history);
                }
            }
            return channelInfo;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class SearchRequestTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        SearchRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String queryStr = params[0];
                return getDataFromApi(queryStr);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getDataFromApi(String queryStr) throws IOException {
            YouTube.Search.List search = mService.search().list(Collections.singletonList("id,snippet"));

            search.setQ(queryStr);

            search.setType(Collections.singletonList("video"));

            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(15L);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                String str = MainActivity.prettyPrint(searchResultList.iterator(), queryStr);
                System.out.println(str);
                return str;
            }
            return "";
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            mOutputText.setText(output);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private void performYoutubeSearch(String queryStr) {
        new SearchRequestTask(mCredential).execute(queryStr);
    }

    private void performYoutubeWl(String videoId) {
        new WLRequestTask(mCredential).execute(videoId);
    }

    private void performYoutubeSub(String channeld) {
        new SubRequestTask(mCredential).execute(channeld);
    }

    private class WLRequestTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        WLRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String videoId = params[0];
                return getDataFromApi(videoId);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getDataFromApi(String videoId) throws IOException {
            PlaylistItem playlistItem = new PlaylistItem();
            PlaylistItemSnippet snippet = new PlaylistItemSnippet();
            ResourceId resourceId = new ResourceId();
            resourceId.setVideoId(videoId);
            resourceId.setKind("youtube#video");
            snippet.setResourceId(resourceId);
            snippet.setPlaylistId("WL");
            snippet.setPosition(20L);
            playlistItem.setSnippet(snippet);
            PlaylistItem responseItem = mService.playlistItems()
                    .insert(Collections.singletonList("snippet"), playlistItem)
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    .execute();

            System.out.println(responseItem);
            /*
            {
                "snippet": {
                    "playlistId": "WL",
                    "resourceId": {
                        "videoId": "",
                        "kind": "youtube#video"
                    },
                    "position": 0
                }
            }
            try {
                URL url = new URL("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet"
                        + "&key=" + ApiKey.YOUTUBE_API_KEY
                        + "&access_token=" + mCredential.getToken());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                String input = "{ \"snippet\": {\"playlistId\": \"WL\",\"resourceId\": {\"videoId\": \"" + videoId + "\",\"kind\": \"youtube#video\"},\"position\": 0}}";

                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                StringBuilder sb = new StringBuilder();
                System.out.println("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }

                conn.disconnect();
                System.out.println(sb);
                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }*/
            return responseItem.toString();
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            mOutputText.setText(output);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                System.out.println(mLastError);
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class SubRequestTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        SubRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String channeld = params[0];
                return getDataFromApi(channeld);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getDataFromApi(String channeld) throws IOException {
            Subscription subscription = new Subscription();

            // Add the snippet object property to the Subscription object.
            SubscriptionSnippet snippet = new SubscriptionSnippet();
            ResourceId resourceId = new ResourceId();
            resourceId.setChannelId("UCsggG8qHVeA-y57I6yMq1tg");
            resourceId.setKind("youtube#subscription");
            snippet.setResourceId(resourceId);
            subscription.setSnippet(snippet);

            YouTube.Subscriptions.Insert request = mService.subscriptions()
                    .insert(Collections.singletonList("snippet"), subscription);
            Subscription response = request.setKey(ApiKey.YOUTUBE_API_KEY).execute();
            System.out.println(response);
            return response.toPrettyString();
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            mOutputText.setText(output);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                System.out.println(mLastError);
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
