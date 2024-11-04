// MyApiService.java
package com.example.diarynotes;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface MyApiService {
    @GET("diaryEntries.json") // Fetch all diary entries
    Call<Map<String, Note>> getDiaryEntries();

    @POST("diaryEntries.json") // Save a new diary entry
    Call<Void> saveDiaryEntry(@Body Note entry);
}
