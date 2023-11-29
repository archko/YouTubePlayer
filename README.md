base on YouTubePlaylist,https://github.com/akoscz/YouTubePlaylist.git
player fork from https://github.com/bertrandmartel/youtubetv.git  fix some bug,destroy webview when exit player

quickstart: https://developers.google.com/youtube/v3/quickstart/android?hl=zh-cn
set keystore, packagename, enable youtube api, then run the project ,enjoy it.
any question,email:archko@gmail.com/archko@sina.com

===============

A sample Android application which demonstrates the use of the [YouTube Data API v3](https://developers.google.com/youtube/v3/).

This sample app makes use of the [YouTube Data API v3 classes](https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/) to fetch a YouTube [Playlist](https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/com/google/api/services/youtube/model/Playlist.html) using the [GetPlaylistAsyncTask](app/src/main/java/com/akoscz/youtube/GetPlaylistAsyncTask.java) which then extracts the list of [Video](https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/com/google/api/services/youtube/model/Video.html)'s the playlist contains.  The list of video's are then presented using a [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) of [CardView](https://developer.android.com/reference/android/support/v7/widget/CardView.html)'s in the [YouTubeRecyclerViewFragment](app/src/main/java/com/akoscz/youtube/YouTubeRecyclerViewFragment.java).  The data binding of video details to CardView is handled by the [PlaylistCardAdapter](app/src/main/java/com/akoscz/youtube/PlaylistCardAdapter.java).

i add search, oauth2, subscription, playlistitems, channels api