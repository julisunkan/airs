package com.airesumebuilder.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit service interface for the Groq Chat Completion API.
 */
public interface GroqApiService {

    String BASE_URL = "https://api.groq.com/openai/v1/";

    @POST("chat/completions")
    Call<GroqResponse> chatCompletion(
            @Header("Authorization") String bearerToken,
            @Body GroqRequest request
    );
}
