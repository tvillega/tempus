# Tempus-ng

## New Features
- **Last.fm Integration**: Added Last.fm scrobble counts to the Now Playing screen.
- **Enhanced Playlist Management**:
    - Multi-playlist selection in the `PlaylistChooserDialog`.
    - Persistent playlist pinning for quick access.
    - Real-time playlist synchronization across Home and Playlist tabs.
    - Client-side sorting options for playlist pages.
    - Ability to remove tracks from playlists with immediate UI updates.
- **Home Screen Improvements**:
    - New "Recently Played" and "Top Played" sections for both Artists and Songs.
    - Added a reorganizable "History" section.
- **Player Enhancements**:
    - Sequential "Play Next" functionality.
    - Searchable playlist chooser and direct "Add to Playlist" button within the player.
    - Dynamic and user-configurable metadata display on the Now Playing screen.
    - Scrobble threshold setting for better Last.fm reporting.
- **Artist Detail Page**:
    - Redesigned with categorized album carousels.
    - Added circular similar artists display.
    - Improved layout for top songs.

## UI & UX Enhancements
- **Modern Navigation**: Implemented a pill-shaped navigation dock for a cleaner, modern look.
- **Redesigned Settings**:
    - New card-based layout for settings.
    - Pill-tab navigation for easier category switching.
- **Refined Player UI**:
    - Polished mini-player design.
    - Adjusted title and artist layout for better readability.
    - Circular artist images throughout the app.
- **Toolbar Optimization**: Search functionality moved from the toolbar to the navigation dock for centralized access.
- **Toolbar Cleanup**: Removed overflow menu (3-dots) from Library and Downloads tabs since settings is accessible from the navigation dock.
- **Track Info in Context Menu**: Added "Track Info" option to the song bottom sheet (3-dots menu), opening the same track info dialog available from the Now Playing screen.
- **Consolidated Feedback**: Improved toast and dialog feedback for user actions.

## Security & Privacy
- **Encrypted Credentials**: Sensitive fields (password, token, salt, Last.fm API key) are now stored in `EncryptedSharedPreferences` (AES256-GCM). Existing values are migrated automatically on first launch.
- **Release Logging Hardened**: All four HTTP clients (Subsonic, Navidrome, GitHub, Last.fm) now log at `BASIC` level in debug builds and `NONE` in release builds, preventing credentials and API responses from leaking to logcat.
- **ContentProvider Input Validation**: `AlbumArtContentProvider` now validates album IDs against the `UriMatcher` and rejects null, empty, or path-traversal values before any file access.
- **Network Security Config**: Removed redundant `android:usesCleartextTraffic="true"` from the manifest (governed by `network_security_config` only). User-installed CA certificates are no longer trusted in release builds; a debug-only override restores them for development.
- **Flavor Isolation**: Google Cast meta-data moved from the main manifest to a tempus-flavor-specific manifest, ensuring the degoogled build has zero Google service dependencies.
- **ProGuard Hardening**: Enabled `-renamesourcefileattribute SourceFile` to strip original filenames from release stack traces. Added keep rules for `security-crypto` and Tink.
- **Debug Log Removal**: Removed `Log.d`/`Log.e` calls from `NavidromeClient` and `Preferences` that were logging server URLs, sort state, and auth-adjacent data unconditionally.
- **Dependency Hygiene**: Downgraded `okhttp3:logging-interceptor` from `5.0.0-alpha.14` to the stable `4.12.0` release.

## Performance
- **Scroll Performance**: Fixed choppy scrolling in large song lists (playlists, history, top songs). `SongHorizontalAdapter` was returning `position` as the view type, preventing RecyclerView from ever recycling ViewHolders — on a 500-song list this meant 500 allocations with zero reuse. Removed the override so RecyclerView recycles correctly.
- **Reduced Per-Item Bind Work**: Star rating drawables (`ic_star`, `ic_star_outlined`) are now loaded once and cached instead of calling `AppCompatResources.getDrawable()` five times per row on every bind. `DownloadTracker` is also cached rather than fetched on each bind.
- **DiffUtil**: Replaced `notifyDataSetChanged()` in `SongHorizontalAdapter` with `DiffUtil`, so data updates (filter, sort, data load) only redraw the rows that actually changed instead of flashing the entire list.
- **ProGuard**: Added missing `-dontwarn` rules for optional Tink dependencies (`com.google.api.client`, `org.joda.time`) that caused R8 to fail on release builds after the `security-crypto` addition.

## System & Architecture
- **Java 17 Upgrade**: Updated the project to utilize Java 17 toolchain and features.
- **Stability**: Fixed various crashes, including a critical mini-player issue.
- **Optimization**: Optimized playlist and UI synchronization for better performance.
- **Degoogled Flavor**: Maintained and improved the Google-free build variant with feature parity.
- **Offline Scrobble Syncing**: Implemented persistence and synchronization for scrobbles made while offline, preserving original timestamps.
- **Cross-App State Propagation**: Favorite, rating, and play count changes now propagate in real-time across all screens (player, queue, song lists, track info dialog) using LiveData event channels.
- **Play Count Accuracy**: Fixed stale play count display caused by LiveData sticky behavior and baked-in media extras. Play counts now update immediately after scrobble, including for songs with zero prior plays.
