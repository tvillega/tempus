package com.cappielloantonio.tempo.navigation;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

public interface NavigationController {

    @OptIn(markerClass = UnstableApi.class)
    default boolean isVisible() {
        return NavigationDelegate.isVisible();
    }
}