# AndroidAuthPack for login with Facebook


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
enable Android platform and get the App IDs for your app
Define the resource string `facebook_application_id` to match the application ID:

```xml
<resources>
    <!-- ... -->
    <string name="facebook_application_id" translatable="false">APP_ID</string>
    <!-- Facebook Application ID, prefixed by 'fb'. Enables Chrome Custom tabs. -->
    <string name="facebook_login_protocol_scheme" translatable="false">fbAPP_ID</string>
</resources>
```
