# Food Recognition from images using Calorie Mama AI API

## What is Calorie Mama API?

*Calorie Mama uses machine learning to identify over one hundred thousand foods, drinks, and packaged goods. It covers many local and global foods.*

Find out more here at [http://www.caloriemama.ai](http://www.caloriemama.ai)

Features:->

Capture food images using the device camera.
Detect food items using the Google Cloud Vision API.
Retrieve nutritional data via the Nutritionix API.
Display formatted nutritional information in a user-friendly format.
Handle unsupported food items with fallback messages

Prerequisites:->

Android device or emulator with camera support.
Internet connection for API calls.
Google Cloud Vision API credentials (Service Account JSON).
Nutritionix API credentials (App ID and App Key).

git clone https://github.com/yourusername/see_food_recognition_with_calorie_mama.git

cd see_food_recognition_with_calorie_mama

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    testImplementation 'junit:junit:4.13.2'
    
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    implementation 'org.json:json:20230227'
    
    implementation 'com.google.code.gson:gson:2.11.0'
    
    implementation 'com.google.cloud:google-cloud-vision:3.34.0'
    
    implementation 'com.google.api-client:google-api-client:2.2.0'
    
    implementation 'io.grpc:grpc-netty:1.63.0'
    
    implementation 'org.conscrypt:conscrypt-android:2.5.2'
    
}


Build and Run:->

Open the project in Android Studio.

Connect an Android device or start an emulator.

Build the project: Build > Rebuild Project.

Run the app: Run > Run 'app'.
