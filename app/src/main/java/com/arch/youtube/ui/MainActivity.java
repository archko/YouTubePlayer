package com.arch.youtube.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arch.youtube.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int REQUEST_PERMISSION = 3;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    public static final String TAG = "TomerBu";
    GoogleAccountCredential credential;
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final String PREF_ACCOUNT_NAME = "accountName";
    Button btnSearch;
    EditText etSearch;
    String API_KEY = "AIzaSyAZ-IQ3_jlb6cCQ2Bkzre1uBZhRKYxh7sg";
    private String mChosenAccountName = "archko@gmail.com";

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ArrayList<String> scopes = new ArrayList<>();
        scopes.add(YouTubeScopes.YOUTUBE);//scopes.add("https://www.googleapis.com/auth/youtube");
        scopes.add(YouTubeScopes.YOUTUBE_UPLOAD);
        scopes.add(YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT);
        //scopes.add(Scopes.PROFILE);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), scopes);
        // set exponential backoff policy
        credential.setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(mChosenAccountName);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        etSearch = (EditText) findViewById(R.id.etSearch);

        findViewById(R.id.btnOauth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth();
            }
        });

        Thread thread = new Thread(this::youtube);
        //thread.start();
    }

    private void youtube() {
        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, request -> {
        }).setApplicationName("YoutubeDemo").build();

        // Prompt the user to enter a query term.
        String queryTerm = "Hello";

        // Define the API request for retrieving search results.
        try {
            YouTube.Search.List search = youtube.search().list(Collections.singletonList("id,snippet"));

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            String apiKey = "AIzaSyBcXsITjWAQI4iNnkxc56nn_wCubSaFoco";
            search.setKey(apiKey);

            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType(Collections.singletonList("video"));

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(25L);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), queryTerm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=============================================================");
        sb.append(
                "   First " + 25 + " videos for search on \"" + query + "\".");
        sb.append("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            sb.append(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                sb.append(" Video Id" + rId.getVideoId());
                sb.append(" Title: " + singleVideo.getSnippet().getTitle());
                sb.append(" Thumbnail: " + thumbnail.getUrl());
                sb.append("\n-------------------------------------------------------------\n");
            }
        }

        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();


        /*new BaseAsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... voids) {
                addSubscription();
                return null;
            }
        }.execute();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions[0].equals(Manifest.permission.GET_ACCOUNTS) && requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addSubscription();
        }
    }

    void addSubscription() {
        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION);
            return;
        }
        try {
            // Authorize the request.
            //  Credential credential = Auth.authorize(scopes, "addsubscription");
            // This object is used to make YouTube Data API requests.
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential).setApplicationName(
                    "YoutubeDemo").build();

            // We get the user selected channel to subscribe.
            // Retrieve the channel ID that the user is subscribing to.
            String channelId = getChannelId();
            System.out.println("You chose " + channelId + " to subscribe.");

            // Create a resourceId that identifies the channel ID.
            ResourceId resourceId = new ResourceId();
            resourceId.setChannelId(channelId);
            resourceId.setKind("youtube#channel");

            // Create a snippet that contains the resourceId.
            SubscriptionSnippet snippet = new SubscriptionSnippet();
            snippet.setResourceId(resourceId);
            snippet.setTitle("Tomer");


            // Create a request to add the subscription and send the request.
            // The request identifies subscription metadata to insert as well
            // as information that the API server should return in its response.
            Subscription subscription = new Subscription();
            subscription.setSnippet(snippet);
            YouTube.Subscriptions.Insert subscriptionInsert =
                    youtube.subscriptions().insert(Collections.singletonList("snippet,contentDetails"), subscription);
            Subscription returnedSubscription = subscriptionInsert.execute();

            // Print information from the API response.
            System.out.println("\n================== Returned Subscription ==================\n");
            System.out.println("  - Id: " + returnedSubscription.getId());
            System.out.println("  - Title: " + returnedSubscription.getSnippet().getTitle());

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    void auth() {
        try {
            startActivityForResult(credential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        } catch (Exception e) {
            Toast.makeText(this, "open google account error:" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(String.format("oauth:%s,:%s", resultCode, data));
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mChosenAccountName = accountName;
                        credential.setSelectedAccountName(accountName);
                        Toast.makeText(this, mChosenAccountName, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //btnSearch Clicked
    @Override
    public void onClick(View view) {
        new Thread(() -> {
            Editable query = etSearch.getText();
            performYoutubeSearch(query.toString());
        }).start();
    }

    private void performYoutubeSearch(String queryStr) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        YouTube youTube = new YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();
        try {
            YouTube.Search.List search = youTube.search().list(Collections.singletonList("id,snippet"));

            //search.setKey("AIzaSyDYL79YVkG_3qGade9Le-6J72qQCPIKCaQ");
            search.setQ(queryStr);

            search.setType(Collections.singletonList("video"));

            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(15L);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), queryStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getChannelId() {
        return "UCtVd0c0tGXuTSbU5d8cSBUg";
    }
}