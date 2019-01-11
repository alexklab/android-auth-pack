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
    implementation "com.github.alexklab.android-auth-pack:facebook:1.0.4"
}
```

On the [Facebook developer dashboard](https://developers.facebook.com) site, 
enable Android platform and get the App IDs for your app. 

Define the string resources to match the application ID:

```xml
<resources>
    <!-- ... -->
    <string name="facebook_application_id" translatable="false">APP_ID</string>
    <!-- Facebook Application ID, prefixed by 'fb'. Enables Chrome Custom tabs. -->
    <string name="facebook_login_protocol_scheme" translatable="false">fbAPP_ID</string>
</resources>
```

Add to your `AndroidManifest.xml` internet `uses-permission` 
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
	<uses-permission android:name="android.permission.INTERNET" />
	<!-- ... -->
</manifest>
```

Add to your `AndroidManifest.xml` Facebook activities and `meta-data`
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
		
		<!-- ... Login activities -->
		<activity
            	    android:name="com.facebook.FacebookActivity"
            	    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
            	    android:label="@string/app_name" />

        	<activity
            	    android:name="com.facebook.CustomTabActivity"
            	    android:exported="true">
            		<intent-filter>
                		<action android:name="android.intent.action.VIEW" />
				<category android:name="android.intent.category.DEFAULT" />
                		<category android:name="android.intent.category.BROWSABLE" />
				<data android:scheme="@string/fb_login_protocol_scheme" />
            		</intent-filter>
        	</activity>
		
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

private fun signIn(){
    facebookSignInService.signIn{ token, bundle, exception ->
    	// TODO store user
    }
}
```
