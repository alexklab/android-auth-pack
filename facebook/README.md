# AndroidAuthPack for login with Facebook

# Configuration 

In your `project/build.gradle` file add a dependency

```groovy
allprojects {
    repositories {
        // ...
	maven { url 'https://jitpack.io' }
    }
}
```

In your `app/build.gradle` file add a dependency on one of the AndroidAuthPack
libraries.

```groovy
dependencies {
    implementation "com.github.alexklab.android-auth-pack:facebook:$last_auth_pack_version"
}
```

On the [Facebook developer dashboard](https://developers.facebook.com) site, 
enable Android platform and get the App ID for your app.

Define the string resources to match the application ID:

```xml
<resources>
	<string name="facebook_application_id" translatable="false">APP_ID</string>
</resources>
```

Add to your `AndroidManifest.xml` internet `uses-permission` 
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
	<uses-permission android:name="android.permission.INTERNET" />
	<!-- ... -->
</manifest>
```

Add to your `AndroidManifest.xml` Facebook activity and `meta-data`
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
	<application
	    android:allowBackup="true"
	    android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:theme="@style/AppTheme">
		
	    <!-- ... Meta-Data -->
	    <meta-data
                android:name="com.facebook.sdk.ApplicationId"
                android:value="@string/facebook_app_id" />
		
		<!-- ... Login activity -->
	    <activity
                android:name="com.facebook.FacebookActivity"
                android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
                android:label="@string/app_name" />
		
	</application>
</manifest>
```

## Usage

Implement View model

Create and use `FacebookSignInService` in your activity

```kotlin
private val facebookSignInService = FacebookSignInService()

override fun onCreate(savedInstanceState: Bundle?) { 	
    facebookSignInService.onCreate(this)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    facebookSignInService.onActivityResult(requestCode, resultCode, data)
}

private fun signIn(){
    facebookSignInService.signInAndFetchProfile{ 

    if(it.profile != null){
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
