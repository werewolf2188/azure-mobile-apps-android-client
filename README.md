# Microsoft Azure Mobile Apps: Android Client SDK

With Microsoft Azure Mobile Apps you can add a scalable backend to your connected client applications in minutes. To learn more, visit our [Developer Center](http://azure.microsoft.com/en-us/develop/mobile).

## Getting Started

If you are new to Mobile Services, you can get started by following our tutorials for connecting your Mobile
Services cloud backend to [Windows Store apps](http://azure.microsoft.com/en-us/documentation/articles/mobile-services-windows-store-get-started/),
and [Windows Phone 8 apps](http://azure.microsoft.com/en-us/documentation/articles/mobile-services-windows-phone-get-started/).

## Download Source Code

To get the source code of our SDKs and samples via **git** just type:

    git clone https://github.com/Azure/azure-mobile-apps-android-client.git
    cd ./azure-mobile-apps-android-client/

## Reference Documentation
- [Android SDK](CHANGELOG.md)

## Android SDK
Microsoft Azure Mobile Services can be used with an Android-based device using our Android SDK. You can get the Android SDK in one of the following two ways or you can download the source code using the instructions above.

1. For an Android studio project, include the line `compile 'com.microsoft.azure:azure-mobile-services-android-sdk:2.0.3'` in the dependencies section of build.gradle file of the app
2. Eclipse users can [download the Android SDK](https://go.microsoft.com/fwLink/?LinkID=280126&clcid=0x409) directly or can download the source code using the instructions above.

### Prerequisites
The SDK requires Android Studio.

### Building and Referencing the SDK
1. Open the folder `\azure-mobile-services\sdk\android` using the option `Open an existing Android Studio Project` in Android Studio.
2. Project should be built automatically, In case it does not build, Right click on `sdk` folder and select `Make Module 'sdk'`
3. The file `sdk-release.aar` should be present at `\azure-mobile-services\sdk\android\src\sdk\build\outputs\aar` 
4. Rename the file `sdk-release.aar` to `sdk-release.zip`
5. Extract the zip file, `classes.jar` should be present in the root folder.

### Running the Tests

The SDK has a suite of unit tests that you can easily run.

1. Open the folder `\azure-mobile-services\sdk\android` using the option `Open an existing Android Studio Project` in Android Studio.
2. Project should be built automatically, In case it does not build, Right click on `sdk` folder and select `Make Module 'sdk.testapp'`
3. Expand `sdk.testapp` and sub folder `java`
4. Right click on `com.microsoft.windowsazure.mobileservices.sdk.testapp`, Select `Run`, Select `Tests in com.microsoft.windowsazure.mobileservices.sdk.testapp` (with Android tests icon)

## Useful Resources

* [Quickstarts](https://github.com/Azure/azure-mobile-services-quickstarts)
* [Samples](https://github.com/Azure/mobile-services-samples)
* Tutorials and product overview are available at [Microsoft Azure Mobile Services Developer Center](http://azure.microsoft.com/en-us/develop/mobile).
* Our product team actively monitors the [Mobile Services Developer Forum](http://social.msdn.microsoft.com/Forums/en-US/azuremobile/) to assist you with any troubles.

## Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).
If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-mobile-services/issues) section of the project.