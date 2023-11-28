package com.arch.youtube.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arch.youtube.R;
import com.arch.youtube.common.YoutubeApiHolder;
import com.arch.youtube.task.GetPlaylistsByChannelTask;
import com.bumptech.glide.Glide;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class YouTubePlaylistFragment extends Fragment {
    public static final String CHANNEL_ID = "channelId";
    private String channelId;
    private RecyclerView mRecyclerView;
    private List<Playlist> playlist = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private YouTube mYouTubeDataApi;
    private PlaylistAdapter mPlaylistAdapter;

    public static YouTubePlaylistFragment newInstance(String channelId) {
        YouTubePlaylistFragment fragment = new YouTubePlaylistFragment();
        Bundle args = new Bundle();
        args.putString(CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }

    public YouTubePlaylistFragment() {
        mYouTubeDataApi = YoutubeApiHolder.holder.mYoutubeApi;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            channelId = getArguments().getString(CHANNEL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.youtube_playlist_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.youtube_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        initCardAdapter();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        reloadUi();
    }

    private void reloadUi() {
        new GetPlaylistsByChannelTask(mYouTubeDataApi, channelId) {
            @Override
            public void onPostExecute(PlaylistListResponse result) {
                handleGetPlaylistResult(result);
            }
        }.execute();
    }

    private void initCardAdapter() {
        mPlaylistAdapter = new PlaylistAdapter();
        mRecyclerView.setAdapter(mPlaylistAdapter);
    }

    private void handleGetPlaylistResult(PlaylistListResponse result) {
        if (result == null) return;
        playlist.clear();
        playlist.addAll(result.getItems());
        mPlaylistAdapter.notifyDataSetChanged();
    }

    public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final Context mContext;
            public final TextView mTitleText;
            public final TextView mDescriptionText;
            public final ImageView mThumbnailImage;

            public ViewHolder(View v) {
                super(v);
                mContext = v.getContext();
                mTitleText = (TextView) v.findViewById(R.id.video_title);
                mDescriptionText = (TextView) v.findViewById(R.id.video_description);
                mThumbnailImage = (ImageView) v.findViewById(R.id.video_thumbnail);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_playlist_card, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Playlist video = playlist.get(position);

            PlaylistSnippet videoSnippet = video.getSnippet();

            holder.mTitleText.setText(videoSnippet.getTitle());
            holder.mDescriptionText.setText(videoSnippet.getDescription());

            Glide.with(holder.mThumbnailImage)
                    .load(videoSnippet.getThumbnails().getHigh().getUrl())
                    .placeholder(R.drawable.video_placeholder)
                    .into(holder.mThumbnailImage);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EmptyFragmentActivity.startPlaylistItems(videoSnippet.getTitle(),
                            YouTubePlaylistItemsFragment.class.getName(),
                            video.getId(),
                            getActivity());
                }
            });
        }

        @Override
        public int getItemCount() {
            return playlist.size();
        }

        private boolean isEmpty(String s) {
            if (s == null || s.length() == 0) {
                return true;
            }
            return false;
        }

        private String parseDuration(String in) {
            boolean hasSeconds = in.indexOf('S') > 0;
            boolean hasMinutes = in.indexOf('M') > 0;

            String s;
            if (hasSeconds) {
                s = in.substring(2, in.length() - 1);
            } else {
                s = in.substring(2, in.length());
            }

            String minutes = "0";
            String seconds = "00";

            if (hasMinutes && hasSeconds) {
                String[] split = s.split("M");
                minutes = split[0];
                seconds = split[1];
            } else if (hasMinutes) {
                minutes = s.substring(0, s.indexOf('M'));
            } else if (hasSeconds) {
                seconds = s;
            }

            // pad seconds with a 0 if less than 2 digits
            if (seconds.length() == 1) {
                seconds = "0" + seconds;
            }

            return minutes + ":" + seconds;
        }
    }

}
