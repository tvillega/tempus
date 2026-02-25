package com.cappielloantonio.tempo.ui.activity.base;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cappielloantonio.tempo.R;
import com.cappielloantonio.tempo.ui.activity.MainActivity;
import com.cappielloantonio.tempo.util.Preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class NavigationHelper {
    /* UI components */
    private BottomNavigationView bottomNavigationView;
    private FrameLayout bottomNavigationViewFrame;
    private DrawerLayout drawerLayout;

    /* Navigation components */
    private NavigationView navigationView;
    private NavHostFragment navHostFragment;
    private NavController navController;

    /* States that need to be remembered */
    private final Context context;

    /* Private constructor */
    private NavigationHelper(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /* Call inside onCreate() in MainActivity giving as argument `this` */
    @OptIn(markerClass = UnstableApi.class)
    public static NavigationHelper init(@NonNull MainActivity activity) {
        NavigationHelper helper = new NavigationHelper(activity);
        helper.bindViews(activity);
        helper.setupNavigation(activity);
        return helper;
    }

    /* Call inside onDestroy() in MainActivity*/
    public void release() {
        bottomNavigationView = null;
        bottomNavigationViewFrame = null;
        drawerLayout = null;
        navigationView = null;
        navHostFragment = null;
        navController = null;
    }

    /* Bind the views by finding them on the layout (XML id attr) */
    @OptIn(markerClass = UnstableApi.class)
    private void bindViews(@NonNull MainActivity activity) {
        bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationViewFrame = activity.findViewById(R.id.bottom_navigation_frame);
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        navigationView = activity.findViewById(R.id.nav_view);

        navHostFragment = (NavHostFragment) activity
                .getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(navHostFragment).getNavController();
    }

    /* The navigation graph (untouched original implementation) */
    @OptIn(markerClass = UnstableApi.class)
    private void setupNavigation(@NonNull MainActivity activity) {
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    int destId = destination.getId();
                    boolean isTarget = destId == R.id.homeFragment ||
                            destId == R.id.libraryFragment ||
                            destId == R.id.downloadFragment;

                    if (isTarget &&
                            activity.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    /*
    Clean public methods
    Removes the need to invoke the activity on the fragment
     */

    public void setBottomNavigationBarVisibility(boolean visible) {
        int visibility = visible
                ? View.VISIBLE
                : View.GONE;
        bottomNavigationView.setVisibility(visibility);
        bottomNavigationViewFrame.setVisibility(visibility);
    }

    public void setNavigationDrawerLock(boolean locked) {
        int mode = locked
                ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(mode);
    }

    public void toggleNavigationDrawerLockOnOrientationChange(MainActivity activity, boolean isLandscape) {
        if (Preferences.getEnableDrawerOnPortrait()) {
            setNavigationDrawerLock(false);
            return;
        }
        setNavigationDrawerLock(!isLandscape);
    }

    /*
    All of these are the "backward compatible" changes that don't break the assumption
    that everything was defined on the activity and is gobally available
     */

    @NonNull
    public NavController getNavController() {
        return navController;
    }

    @NonNull
    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    @NonNull
    public FrameLayout getBottomNavigationViewFrame() {
        return bottomNavigationViewFrame;
    }

    @NonNull
    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    /*
    Auxiliar functions, could be moved somewhere else
     */

    @OptIn(markerClass = UnstableApi.class)
    public void setSystemBarsVisibility(MainActivity activity, boolean visibility) {
        WindowInsetsControllerCompat insetsController;
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        insetsController = new WindowInsetsControllerCompat(window, decorView);

        if (visibility) {
            WindowCompat.setDecorFitsSystemWindows(window, true);
            insetsController.show(WindowInsetsCompat.Type.navigationBars());
            insetsController.show(WindowInsetsCompat.Type.statusBars());
            insetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            insetsController.hide(WindowInsetsCompat.Type.navigationBars());
            insetsController.hide(WindowInsetsCompat.Type.statusBars());
            insetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
