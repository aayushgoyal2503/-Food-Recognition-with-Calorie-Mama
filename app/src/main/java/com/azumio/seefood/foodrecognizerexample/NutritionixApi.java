package com.azumio.seefood.foodrecognizerexample;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NutritionixApi {
    private static final String API_URL = "https://trackapi.nutritionix.com/v2/natural/nutrients";
    private static final String APP_ID = "ed256103";
    private static final String APP_KEY = "f553ab4b94410dd5e119b12a254bbe7d";

    public static String getNutritionData(String foodQuery) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject json = new JSONObject();
        try {
            json.put("query", foodQuery);
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error creating JSON: " + e.getMessage();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("x-app-id", APP_ID)
                .addHeader("x-app-key", APP_KEY)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                return parseNutritionData(jsonResponse);
            } else {
                return "API request failed: " + response.code() + " - " + response.message();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static String parseNutritionData(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray foods = json.getJSONArray("foods");
            if (foods.length() > 0) {
                JSONObject food = foods.getJSONObject(0);
                String foodName = food.optString("food_name", "Unknown");
                double servingQty = food.optDouble("serving_qty", 1.0);
                String servingUnit = food.optString("serving_unit", "unit");
                double calories = food.optDouble("nf_calories", 0.0);
                double totalFat = food.optDouble("nf_total_fat", 0.0);
                double carbohydrates = food.optDouble("nf_total_carbohydrate", 0.0);
                double protein = food.optDouble("nf_protein", 0.0);

                StringBuilder result = new StringBuilder();
                result.append("Food: ").append(foodName).append("\n");
                result.append("Serving: ").append(String.format("%.1f", servingQty)).append(" ").append(servingUnit).append("\n");
                result.append("Calories: ").append(String.format("%.1f", calories)).append(" kcal\n");
                result.append("Total Fat: ").append(String.format("%.1f", totalFat)).append(" g\n");
                result.append("Carbohydrates: ").append(String.format("%.1f", carbohydrates)).append(" g\n");
                result.append("Protein: ").append(String.format("%.1f", protein)).append(" g");

                return result.toString();
            } else {
                return "No nutritional data available";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error parsing JSON: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String result = getNutritionData("1 cup of pasta");
        System.out.println(result);
    }
}