package com.azumio.seefood.foodrecognizerexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String TAG = "MainActivity";
    private ImageView foodImage;
    private Button captureButton;
    private TextView resultText;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foodImage = findViewById(R.id.food_image);
        captureButton = findViewById(R.id.capture_button);
        resultText = findViewById(R.id.result_text);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        captureButton.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            foodImage.setImageBitmap(imageBitmap);
            analyzeImage(imageBitmap);
        }
    }

    private void analyzeImage(Bitmap bitmap) {
        try {
            // Service account JSON
            String serviceAccountJson = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"nutrition-456606\",\n" +
                    "  \"private_key_id\": \"a29b60ffcb2cd4fb92eb0d32b88cb8174c32c37c\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrSPoJtytPsX2z\\nlkrwRpwhVdk26mONFpV5q5ayIiD0IM8biaM9Z1RSbw+/tchmt7BnDMnJbdWKFu2G\\neFHrjNR0TuFMJ95rqcqH5WhmHXoYln1Ynfu62Yec24zUONV5vj1Jv+wy8RUZUV6S\\nZcfOblAuL7h/ntUsKtRkFJy7HH6RBNm5vJclttamp1bDOr0Yp16WoCW9+6ua8bhz\\nYzgLcb836/dQ8+4VBOHNT/0crwv5r30vErL3rwjBZFnYa1EhhPjPAMSyRwoMo6GQ\\n0gsMRZMWmLdzeL1f/ExiOGjt4wy0obJ75s9Pe5d/ZzpmdDxOE2oalL+JkybtUS5w\\nPkU5mkurAgMBAAECggEAGdFtswIzmpcX6fZ5BD+4cAPnMnJhIBar4EOfeulPC01Y\\nun9kJjDJkoVgG9cURntgsR/J2L1gBykOk4rWWkmHiqKLLjYCPjtBJnvLYT0HNuaU\\nLUPe+lqoqNmlICYS839b8J5nxYP2WLMdrLZ/yBtzpYqW5lG0MyyfiBhdSYzxfP08\\n8ZoOmtCx3rpYocyOeg03iU5kI/1giAuiQkxv9PWgSSloaY9CFqdDgp6nrKIxw8XP\\nH97kJU/OZbeGue8IOWn0/KBqzWXmfbsfWNcfxyqZ8w+EdKyuJHY/6O5SWQ1HpdEp\\n5l76Pcx0EHxO9hIRVYPG0Q3f+OJLSxN9f9ngsDQy+QKBgQDwltoj41OoH/JK4Dpn\\ncR9UyJzWuY86TEnJh5PMPrBUPblWrVORAXaGLp0mmD5Lq464rsVuDkZTDuauzkEJ\\nzg5MFRW8hHtZBhpVp0Om3VPrPpBHhoGQXZq9CbS5LOTvArAjh9qtcMuLcTTqeuzZ\\nbVFRbUg2pADZQp9hjciFmgxFowKBgQC2Qa8yv2FJAA+YuvHrNNBxMQXFnqfM0IzK\\n42VRqtgCYKJbhmp7lJpdfSJyFvq8zrqF7NHmqsYnQW0iyxg5jb3M9lCvUlSTonng\\nCuwfUe6iMLOXbYnidiVFt4nIQnkTrS4dpxIJuRfW9R0+qfNWS03mBIyibd0kE6fE\\n8iDw7JbyWQKBgAXydKn2cJbzUzXunQL5lOCvpNDZ/WRfhmNo6opd8lXiwLYXyr0G\\n4Dso49GUadXNA9Yk29SAndRnxeQETS9E3K2cx9DFJZdpwzTi0ZVKW0yxWzepZ4J+\\nDdkmaEU7it0tn9UBArLO2vkzby9sonYtcZklrmdLXooAT5hFvPMwf6UpAoGBAJEr\\nZopMaAgGdWXY2iLX0YOnYYqUIeIA53Qfhi9znlTeddPjD+PZbM80ggBQD23eWDGe\\nfV52/hh8g5poHKaTIPAl7gSH1ng1vU/YQ1V7JFGp6xKdnGt2YZ/TXnrVf61NjuLt\\nlW+LL9L3MFEee4Vru+OQEhi6258FzOcRWW2tdVoZAoGAS/A88g71kD/s32jfSSD/\\nwYLnbOpALh+5X4qVmeNahs71/k41XDWVQYHJtqTL+2OzyP6goDcx3ad4dJ6+XaIf\\nQc+klsGQolwPm5cDNqHFZgpQmdPRvh24pb8NMb2QN/GGSxzbwWvbC8tParuI5pUI\\nMeiJXOiENEVDaCynZQVNXCM=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"nutritionbot@nutrition-456606.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"101694189388896707855\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/nutritionbot%40nutrition-456606.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            // Convert JSON string to InputStream
            InputStream inputStream = new ByteArrayInputStream(serviceAccountJson.getBytes());

            // Set up credentials
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-vision");
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            ImageAnnotatorClient visionClient = ImageAnnotatorClient.create(settings);

            // Convert bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageData = stream.toByteArray();
            ByteString imgBytes = ByteString.readFrom(new java.io.ByteArrayInputStream(imageData));

            // Build the image and request
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build();
            List<AnnotateImageRequest> requests = ImmutableList.of(request);

            // Perform annotation
            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            StringBuilder visionResult = new StringBuilder();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    Log.e(TAG, "Error: " + res.getError().getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(this, "Vision API error: " + res.getError().getMessage(), Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    visionResult.append(annotation.getDescription()).append(": ").append(String.format("%.2f%%", annotation.getScore() * 100)).append("\n");
                }
            }

            if (visionResult.length() > 0) {
                String[] lines = visionResult.toString().split("\n");
                String topFood = "unknown";
                if (lines.length > 0) {
                    topFood = lines[0].split(":")[0].trim().toLowerCase();
                }

                // Create a final copy of topFood
                final String finalTopFood = topFood;

                new Thread(() -> {
                    // Add logging for debugging
                    Log.d(TAG, "Trying query: 1 cup of " + finalTopFood);
                    String nutritionResponse = NutritionixApi.getNutritionData("1 cup of " + finalTopFood);
                    if (nutritionResponse.startsWith("API request failed: 404")) {
                        Log.d(TAG, "Fallback query: 1 " + finalTopFood);
                        nutritionResponse = NutritionixApi.getNutritionData("1 " + finalTopFood);
                    }
                    if (nutritionResponse.startsWith("API request failed: 404")) {
                        Log.d(TAG, "Second fallback query: 1 serving of " + finalTopFood);
                        nutritionResponse = NutritionixApi.getNutritionData("1 serving of " + finalTopFood);
                    }
                    // Capture the final response as a final variable for the lambda
                    final String finalNutritionResponse = nutritionResponse;
                    runOnUiThread(() -> {
                        String finalResult = visionResult.toString() + "\n\nNutritional Data:\n" +
                                (finalNutritionResponse.startsWith("API request failed") || finalNutritionResponse.startsWith("Error") ?
                                        "No nutritional data available for " + finalTopFood : finalNutritionResponse);
                        resultText.setText(finalResult.length() > 0 ? finalResult : "No data available");
                    });
                }).start();
            } else {
                runOnUiThread(() -> resultText.setText("No food detected"));
            }

            visionClient.close();
        } catch (IOException e) {
            Log.e(TAG, "Image analysis failed: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(this, "Image analysis failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }
}