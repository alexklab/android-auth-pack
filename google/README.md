# AndroidAuthPack for login with Google

Before you can start integrating Google Sign-In in your own app, you must configure a Google API
Console project and set up your Android Studio project

See [Integrating Google Sign-In into Your Android App](https://developers.google.com/identity/sign-in/android/sign-in)
for more details

Open the [Credentials page](https://console.developers.google.com/apis/credentials) in the API Console.
The `Web application` type client ID is your backend server's OAuth 2.0 client ID.
Pass this client to constructor of `GoogleSignInService` when you create the object.

# Configuration

As part of enabling Google APIs services in your Android application you may have
to add the `google-services` plugin to your `build.gradle` file:

In your `project/build.gradle` file add a dependency

```groovy
dependencies {
    classpath 'com.google.gms:google-services:4.2.0'
    // ...
}

allprojects {
    repositories {
        // ...
        google()
	maven { url 'https://jitpack.io' }
    }
}
```

In your `app/build.gradle` file add a dependency on one of the AndroidAuthPack
libraries.

```groovy
dependencies {
    implementation "com.github.alexklab.android-auth-pack:google:$last_auth_pack_version"
}

// Add to the bottom of the file
apply plugin: 'com.google.gms.google-services'
```

## Usage

Implement View model

Create and use `GoogleSignInService` in your activity

```kotlin
private val googleSignInService = GoogleSignInService(Config.WEB_CLIENT_ID)

override fun onCreate(savedInstanceState: Bundle?) { 	
    googleSignInService.onCreate(this)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googleSignInService.onActivityResult(requestCode, resultCode, data)
}

private fun signIn(){
    googleSignInService.signIn{ (profile, e, errType) ->

    if(profile != null){
    	/*
    	 TODO: store user profile
    	 profile.id - unique user facebookId
         profile.name - user name (by default 'firstName + lastName')
         profile.firstName - user first name
         profile.lastName - user last name
         profile.email - user email
         profile.picture - user logo url
    	*/
    } else {
        // TODO handle errType, (AUTH_CANCELED or AUTH_SERVER_ERROR)
    }
}
```
