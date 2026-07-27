package com.airesumebuilder.network;

import android.content.Context;
import android.util.Log;

import com.airesumebuilder.security.SecurityHelper;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton HTTP client for the Groq AI API.
 *
 * <p>Retrieves the API key from {@link SecurityHelper} (EncryptedSharedPreferences),
 * so no key is ever hardcoded.</p>
 */
public class GroqClient {

    private static final String TAG = "GroqClient";

    /** Callback interface returned to callers. */
    public interface AiCallback {
        void onSuccess(String content);
        void onError(String errorMessage);
    }

    private static GroqClient instance;

    private final GroqApiService  apiService;
    private final SecurityHelper  securityHelper;
    private final PreferenceManager prefs;

    private GroqClient(Context context) {
        securityHelper = new SecurityHelper(context);
        prefs          = new PreferenceManager(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                msg -> Log.d(TAG, msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GroqApiService.BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(GroqApiService.class);
    }

    public static synchronized GroqClient getInstance(Context context) {
        if (instance == null) {
            instance = new GroqClient(context.getApplicationContext());
        }
        return instance;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Sends a single-turn prompt and returns the response via callback.
     *
     * @param systemPrompt  System-level instructions (may be null).
     * @param userMessage   The user's prompt.
     * @param callback      Invoked on the calling thread (use Handler for UI updates).
     */
    public void complete(String systemPrompt, String userMessage, AiCallback callback) {
        String apiKey = securityHelper.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not set. Go to Settings → AI Settings.");
            return;
        }

        List<GroqRequest.Message> messages;
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages = Arrays.asList(
                    GroqRequest.Message.system(systemPrompt),
                    GroqRequest.Message.user(userMessage));
        } else {
            messages = Arrays.asList(GroqRequest.Message.user(userMessage));
        }

        GroqRequest request = new GroqRequest(
                prefs.getAiModel(),
                messages,
                prefs.getAiTemperature(),
                prefs.getAiMaxTokens(),
                false  // streaming not supported with basic Retrofit call
        );

        apiService.chatCompletion("Bearer " + apiKey, request)
                .enqueue(new Callback<GroqResponse>() {
                    @Override
                    public void onResponse(Call<GroqResponse> call,
                                           Response<GroqResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GroqResponse body = response.body();
                            if (body.hasError()) {
                                callback.onError(body.getErrorMessage());
                            } else {
                                String content = body.getFirstContent();
                                if (content != null) {
                                    callback.onSuccess(content.trim());
                                } else {
                                    callback.onError("Empty response from AI.");
                                }
                            }
                        } else {
                            int code = response.code();
                            if (code == 401) {
                                callback.onError("Invalid API key (401). Please check Settings.");
                            } else if (code == 429) {
                                callback.onError("Rate limit reached (429). Please wait and retry.");
                            } else {
                                callback.onError("API error " + code + ". Please try again.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GroqResponse> call, Throwable t) {
                        Log.e(TAG, "Network failure", t);
                        if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                            callback.onError("Request timed out. Check your connection.");
                        } else {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    }
                });
    }

    /**
     * Tests whether the saved API key is valid.
     */
    public void testApiKey(AiCallback callback) {
        complete("You are a helpful assistant.",
                "Reply with exactly: API key is valid",
                callback);
    }
}
