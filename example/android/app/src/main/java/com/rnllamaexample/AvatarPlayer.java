package com.rnllamaexample;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayerForActivityOrService;

public class AvatarPlayer extends UnityPlayerForActivityOrService {

    public AvatarPlayer(Context context, FrameLayout parent) {
        super(context);

        View unityView = this.getView();
        ((ViewGroup)unityView.getParent()).removeView(unityView);
        parent.addView(unityView);
    }
}
