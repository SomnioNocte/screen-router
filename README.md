# Screen Router

Screen Navigator for Jetpack Compose.

### Another one?

Yes, and before you say that, just show you my only argument in favor of this navigator, and if it doesn't make sense to you, you can skip it since in other aspects it's definitely not better than the others.

Here's an example from my application (in development): 

https://github.com/user-attachments/assets/15a283fa-5ef2-4403-b01c-f1094029cc23

### How to use

Apparently you need to enable predictive gesture support in the manifest:

```xml
<application
    android:enableOnBackInvokedCallback="true">
    ...
</application>
```

Define some screens:

```kotlin
object Screens {
    data class User(val id: String) : Screen
    
    object Settings : Screen {
        object General : Screen
        object Theme : Screen
    }
}
```

And use a router that will transmit a screen with the type defined earlier.
`NullScreen` is the initial screen, which obviously has no arguments and after which the user's home screen logically follows.

```kotlin
setContent {
    MyAppTheme {
        ScreenRouter { screen ->
            when (screen) {
                is NullScreen -> MainScreen()
                is Screens.User -> UserScreen(screen.id)
                is Screens.Settings -> SettingsScreen()
                is Screens.Settings.General -> GeneralSettingsScreen()
                is Screens.Settings.Theme -> ThemeSettingsScreen()
            }
        }
    }
}
```

### Install

Add JitPack repository in your settings.gradle.kts:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.SomnioNocte:screen-router:0.8.0")
}
```

### TODO

Maybe I should make the API more extensive since there are no customizations at the moment.