package fr.bmartel.youtubetv.media;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arch.youtube.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.model.VideoQuality;

public class VideoPlayerFragment extends Fragment {

    public static final String TAG = "VideoPlayerFragment";
    private YoutubeTvView youtubeTvView;
    private String videoId;
    private String playlistId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            videoId = getArguments().getString("videoId");
            playlistId = getArguments().getString("playlistId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.youtubetv_fragment_view, container, false);
        youtubeTvView = root.findViewById(R.id.youtubetv_view);

        //Video video = MainPlayerController.instance(getContext()).getPendingVideo();

        youtubeTvView.updateView(videoId, playlistId);

        youtubeTvView.playVideo(videoId);

        return root;
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        youtubeTvView.closePlayer();
        youtubeTvView.destroy();
    }

    public void setPlaybackQuality(VideoQuality videoQuality) {
        youtubeTvView.setPlaybackQuality(videoQuality);
    }

    public void closePlayer() {
        youtubeTvView.closePlayer();
    }
}
