package org.haguest;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HAHttpClient {
    private static final int TIMEOUT_MS = 500;

    public interface Callback {
        void onSuccess(int statusCode);
        void onError(Exception error);
    }

    private final ExecutorService executor;
    private final Handler mainHandler;

    public HAHttpClient() {
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Constructor for testing / injection
    public HAHttpClient(ExecutorService executor, Handler mainHandler) {
        this.executor = executor;
        this.mainHandler = mainHandler;
    }

    // ---------------- Light Payload Generators ---------------- //

    public static String buildLightPayload(String target, String action, String level) {
        try {
            JSONObject json = new JSONObject();
            json.put("domain", "light");
            json.put("target", target);
            json.put("action", action);
            if (level != null && !level.isEmpty()) {
                json.put("level", level);
            }
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static String buildLightToggle(String target) {
        return buildLightPayload(target, "toggle", null);
    }

    public static String buildLightOff(String target) {
        return buildLightPayload(target, "off", null);
    }

    public static String buildLightPreset(String target, String level) {
        return buildLightPayload(target, "preset", level);
    }

    // ---------------- Climate Payload Generators ---------------- //

    public static String buildClimateTempDelta(String target, double delta) {
        try {
            JSONObject json = new JSONObject();
            json.put("domain", "climate");
            json.put("target", target);
            json.put("action", "temp_delta");
            json.put("value", delta);
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static String buildClimateSetTemp(String target, double temp) {
        try {
            JSONObject json = new JSONObject();
            json.put("domain", "climate");
            json.put("target", target);
            json.put("action", "set_temp");
            json.put("value", temp);
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static String buildClimateSetMode(String target, String mode) {
        try {
            JSONObject json = new JSONObject();
            json.put("domain", "climate");
            json.put("target", target);
            json.put("action", "set_mode");
            json.put("mode", mode);
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    // ---------------- HTTP Dispatch ---------------- //

    public void post(final String webhookUrl, final String jsonPayload, final Callback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int statusCode = executePostSync(webhookUrl, jsonPayload);
                    postSuccess(callback, statusCode);
                } catch (final Exception e) {
                    postError(callback, e);
                }
            }
        });
    }

    public static int executePostSync(String targetUrl, String jsonPayload) throws Exception {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            byte[] outputBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(outputBytes.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(outputBytes);
                os.flush();
            }

            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    private void postSuccess(final Callback callback, final int statusCode) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(statusCode);
                }
            });
        } else {
            callback.onSuccess(statusCode);
        }
    }

    private void postError(final Callback callback, final Exception error) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(error);
                }
            });
        } else {
            callback.onError(error);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
