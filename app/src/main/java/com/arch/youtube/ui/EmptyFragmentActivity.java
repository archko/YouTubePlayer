package com.arch.youtube.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.arch.youtube.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import fr.bmartel.youtubetv.media.VideoPlayerFragment;

/**
 * @version 1.00.00
 * @description: 一个FragmentActivity，用于启动不同的Fragment。
 * @author: archko 12-12-3
 */
public class EmptyFragmentActivity extends AppCompatActivity {

    public static void startPlaylistItems(String title, String fragmentName, String playlistId, Context context) {
        Intent intent = new Intent(context, EmptyFragmentActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("fragment_class", fragmentName);
        intent.putExtra("playlistId", playlistId);
        context.startActivity(intent);
    }

    public static void startPlaylists(String fragmentName, String channelId, Context context) {
        Intent intent = new Intent(context, EmptyFragmentActivity.class);
        intent.putExtra("fragment_class", fragmentName);
        intent.putExtra("channelId", channelId);
        context.startActivity(intent);
    }

    public static void startPlayer(String fragmentName, String videoId, String playlistId, Context context) {
        Intent intent = new Intent(context, EmptyFragmentActivity.class);
        intent.putExtra("fragment_class", fragmentName);
        intent.putExtra("videoId", videoId);
        intent.putExtra("playlistId", playlistId);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_fragment_activity);
        initFragment();
    }

    public void initFragment() {
        Intent intent = getIntent();
        if (null == intent) {
            finish();
            return;
        }

        String title = intent.getStringExtra("title");
        String className = intent.getStringExtra("fragment_class");

        if (TextUtils.equals(className, YouTubePlaylistItemsFragment.class.getName())) {
            String playlistId = intent.getStringExtra("playlistId");
            Fragment fragment = YouTubePlaylistItemsFragment.newInstance(new String[]{playlistId});
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            return;
        }
        if (TextUtils.equals(className, YouTubePlaylistFragment.class.getName())) {
            String channelId = intent.getStringExtra("channelId");
            Fragment fragment = YouTubePlaylistFragment.newInstance(channelId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            return;
        }
        if (TextUtils.equals(className, VideoPlayerFragment.class.getName())) {
            String videoId = intent.getStringExtra("videoId");
            String playlistId = intent.getStringExtra("playlistId");
            Fragment fragment = new VideoPlayerFragment();
            Bundle args = new Bundle();
            args.putString("videoId", videoId);
            args.putString("playlistId", playlistId);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            return;
        }

        try {
            Fragment old = getSupportFragmentManager().findFragmentById(R.id.container);
            if (null == old) {
                Fragment newFragment = Fragment.instantiate(this, className);
                Bundle args = intent.getExtras();
                if (null != args) {
                    newFragment.setArguments(args);
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.container, newFragment).commit();
            } else {
                Fragment newFragment = Fragment.instantiate(this, className);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, newFragment);
                //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                //ft.addToBackStack(null);
                /*if (bundle.getBoolean("add_to_back_stack", false)) {
                    ft.addToBackStack(null);
                }*/
                ft.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}