package com.hdb.avatar;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayerForActivityOrService;

public class AvatarPlayer extends UnityPlayerForActivityOrService {

    private static final String TAG = "AvatarPlayer";

    // for Unity
    private static AvatarPlayer mInstance;
    public static AvatarPlayer getInstance() {
        return mInstance;
    }

    private IAvatarPlayerEvents mAvatarPlayerEvents;

    public AvatarPlayer(Context context, FrameLayout parent) {
        super(context);

        mInstance = this;

        View unityView = this.getView();
        ((ViewGroup)unityView.getParent()).removeView(unityView);
        parent.addView(unityView);
    }

    public AvatarPlayer(Context context, FrameLayout parent, IAvatarPlayerEvents events) {
        this(context, parent);

        mAvatarPlayerEvents = events;
    }

    public void loadAvatar(String url) {
        UnitySendMessage("AvatarSystem", "LoadAvatar", url);
    }

    // for Unity
    private void onLoadAvatarComplete(boolean success) {
        Log.d(TAG, "onLoadAvatarComplete " + String.valueOf(success));

        if (mAvatarPlayerEvents != null) {
            mAvatarPlayerEvents.onLoadAvatarComplete(success);
        }
    }

    public void speak(String audioUrl, EmotionType emotionType, boolean queue) {
        String param = String.format("{" +
                "\"audioUrl\": \"%s\", " +
                "\"emotionType\": \"%s\", " +
                "\"queue\": \"%b\"" +
                "}", audioUrl, emotionType, queue);
        Log.d(TAG, "speak " + param);
        UnitySendMessage("AvatarSystem", "SpeakWithStrParam", param);
    }

    public void stopSpeaking() { UnitySendMessage("AvatarSystem", "StopSpeaking", ""); }
}
