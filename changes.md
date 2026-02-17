# Tempus NG - Recent Changes

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
- **Consolidated Feedback**: Improved toast and dialog feedback for user actions.

## System & Architecture
- **Java 17 Upgrade**: Updated the project to utilize Java 17 toolchain and features.
- **Stability**: Fixed various crashes, including a critical mini-player issue.
- **Optimization**: Optimized playlist and UI synchronization for better performance.
- **Degoogled Flavor**: Maintained and improved the Google-free build variant with feature parity.
- **Offline Scrobble Syncing**: Implemented persistence and synchronization for scrobbles made while offline, preserving original timestamps.
