package com.documat.client.service;

import com.documat.client.model.AuthResponse;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static String jwtToken = null;

    public static AuthResponse login(String username, String password) throws IOException {
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = gson.fromJson(response.body().string(), AuthResponse.class);
                jwtToken = authResponse.getToken();
                return authResponse;
            } else {
                throw new IOException("Login failed: " + response.code());
            }
        }
    }

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void logout() {
        jwtToken = null;
    }

    public static boolean isAuthenticated() {
        return jwtToken != null;
    }

    public static String getAuthorizationHeader() {
        return "Bearer " + jwtToken;
    }
}
