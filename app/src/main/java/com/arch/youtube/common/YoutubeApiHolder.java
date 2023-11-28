package com.arch.youtube.common;

import com.google.api.services.youtube.YouTube;

/**
 * @author: archko 2023/11/27 :18:33
 */
public class YoutubeApiHolder {

    public YouTube mYoutubeApi;

    public static YoutubeApiHolder holder = Factory.instance;

    private static class Factory {
        static YoutubeApiHolder instance = new YoutubeApiHolder();
    }
}
