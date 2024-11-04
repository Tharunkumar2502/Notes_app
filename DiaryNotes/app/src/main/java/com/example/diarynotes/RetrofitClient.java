package com.example.diarynotes;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL =
            "https://console.firebase.google.com/project/diaryproject-385ea/database/diaryproject-385ea-default-rtdb/data/~2F/"; // Replace with your Firebase Realtime Database URL

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
