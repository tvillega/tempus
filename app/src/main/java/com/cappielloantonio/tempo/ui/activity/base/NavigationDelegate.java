package com.cappielloantonio.tempo.ui.activity.base;

import android.view.View;

import androidx.media3.common.util.UnstableApi;

import com.cappielloantonio.tempo.databinding.ActivityMainBinding;
import com.cappielloantonio.tempo.ui.activity.MainActivity;

/*
 The goal of this class is to stop instanciating MainActivity on each fragment */
@UnstableApi
public final class NavigationDelegate {

    private static final NavigationDelegate INSTANCE = new NavigationDelegate();

    private MainActivity activity;
    private boolean visible = true;

    private NavigationDelegate() {}

    public static NavigationDelegate getInstance() {
        return INSTANCE;
    }

    /* Call inside onCreate() in MainActivity giving as argument `this` */
    public void bind(MainActivity activity) {
        this.activity = activity;
    }

    /* Call inside onDestroy() in MainActivity*/
    public void unbind() {
        this.activity = null;
    }

    public static boolean isVisible() {
        return INSTANCE.visible;
    }

    /** Change visibility and update the UI on the UI thread. */
    public void setVisibility(final boolean visible) {
        this.visible = visible;
        ActivityMainBinding bind = activity.getBinding();
        bind.bottomNavigation.setVisibility(visible
                            ? View.VISIBLE
                            : View.GONE);
    }
}
