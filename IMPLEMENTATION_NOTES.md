# FindersKeepers update

## Implemented
- Replaced the static floor-plan room picker with a customizable room list.
- Users can add a custom room, select it, or remove rooms. The picker scrolls when the room list grows.
- Replaced the external camera intent with an in-app CameraX preview and a Capture photo button.
- Captured images continue to be stored through the existing FileProvider-backed app-private `images/` directory.
- Organized app code into packages:
  - `data.local`: Room database, DAO, and entity
  - `ui.additem`: add-item ViewModel and related state
  - `ui.screens`: dashboard and Compose screens
  - `ui.theme`: Compose theme
- Added CameraX camera2, lifecycle, and view dependencies.

## Notes
- Room choices are currently held in the ViewModel state and reset to the default list when the app process is recreated.
- Build verification could not be completed in this environment because the Gradle wrapper distribution could not be downloaded (network unavailable). Please sync Gradle and build/run on your Android Studio setup.


## Finals Workbook implementation (through Application Architecture)

- **Coroutines:** Room writes/deletes and Retrofit requests run from `viewModelScope`; DAO insert/update functions are suspending functions.
- **Networking and JSON:** `data/remote/RemotePostsApi.kt` declares a Retrofit `GET posts` endpoint; `RemotePost.kt` models JSON; `RetrofitClient.kt` configures Retrofit with Gson. The Dashboard has an optional “Load sample API posts” action using the public JSONPlaceholder sample API. These generic sample posts demonstrate API/JSON handling; they are not user inventory data or curated organization advice.
- **Loading/error/success states:** `domain/model/Resource.kt` defines `Loading`, `Success`, and `Error`; the sample-post dialog shows loading, result, or failure and supports retry.
- **Repository and app architecture:** `data/repository/FindersKeepersRepository.kt` is the ViewModel's single entry point to Room inventory data and the remote API. UI calls the ViewModel, the ViewModel calls the repository, and the repository coordinates the data sources. Packages separate local data, remote data, repository, domain state, and UI.
- **Explicitly out of scope:** Testing, Hilt dependency injection, and WorkManager were not added, per request.
- **Build status:** A Gradle build could not be verified in this environment because the Gradle wrapper distribution download failed due to unavailable network access. Open the project in Android Studio and sync/build it before submitting.
