# AndroidAuthPack - components for implementation authentication funcionality

Is an open-source library for Android that allows you to quickly implement 
login with Facebook, Google, Firebase. 
Solution based on [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/)

## Usage

Library has separate modules for using Firebase, Facebook, Google, etc. 
To get started, see the individual instructions for each module:

* [Login with Google](google/README.md)
* [Login with Facebook](facebook/README.md)
* [Login with Firebase](firebase/README.md)

## Installation

AndroidAuthPack is published as a collection of libraries separated by the
API they target.

In your `project/build.gradle` file add a dependency

```groovy
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
    }
}
```

In your `app/build.gradle` file add a dependency on one of the AndroidAuthPack
libraries.

```groovy
dependencies {
    // AndroidAuthPack for login with Custom Auth System 
    implementation "com.github.alexklab.android-auth-pack:core:1.0.4"
    
    // AndroidAuthPack for login with Twitter 
    implementation "com.github.alexklab.android-auth-pack:twitter:1.0.4"
    
    // AndroidAuthPack for login with Google
    implementation "com.github.alexklab.android-auth-pack:google:1.0.4"
    
    // AndroidAuthPack for login with Facebook
    implementation "com.github.alexklab.android-auth-pack:facebook:1.0.4"
    
    // AndroidAuthPack for login with Firebase
    implementation "com.github.alexklab.android-auth-pack:firebase:1.0.4"
}
```

## Sample login with Google
// TODO

## Sample login with Facebook
// TODO

## Sample login with Firebase
// TODO
