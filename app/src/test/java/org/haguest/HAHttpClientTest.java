package org.haguest;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HAHttpClientTest {

    @Test
    public void testLightTogglePayload() throws Exception {
        String payload = HAHttpClient.buildLightToggle("desk");
        JSONObject json = new JSONObject(payload);

        assertEquals("light", json.getString("domain"));
        assertEquals("desk", json.getString("target"));
        assertEquals("toggle", json.getString("action"));
        assertFalse(json.has("level"));
    }

    @Test
    public void testLightOffPayload() throws Exception {
        String payload = HAHttpClient.buildLightOff("desk");
        JSONObject json = new JSONObject(payload);

        assertEquals("light", json.getString("domain"));
        assertEquals("desk", json.getString("target"));
        assertEquals("off", json.getString("action"));
    }

    @Test
    public void testLightPresetPayload() throws Exception {
        String payload = HAHttpClient.buildLightPreset("desk", "low");
        JSONObject json = new JSONObject(payload);

        assertEquals("light", json.getString("domain"));
        assertEquals("desk", json.getString("target"));
        assertEquals("preset", json.getString("action"));
        assertEquals("low", json.getString("level"));
    }

    @Test
    public void testClimateTempDeltaPayload() throws Exception {
        String payload = HAHttpClient.buildClimateTempDelta("stat", 1.0);
        JSONObject json = new JSONObject(payload);

        assertEquals("climate", json.getString("domain"));
        assertEquals("stat", json.getString("target"));
        assertEquals("temp_delta", json.getString("action"));
        assertEquals(1.0, json.getDouble("value"), 0.001);
    }

    @Test
    public void testClimateSetTempPayload() throws Exception {
        String payload = HAHttpClient.buildClimateSetTemp("stat", 72.5);
        JSONObject json = new JSONObject(payload);

        assertEquals("climate", json.getString("domain"));
        assertEquals("stat", json.getString("target"));
        assertEquals("set_temp", json.getString("action"));
        assertEquals(72.5, json.getDouble("value"), 0.001);
    }

    @Test
    public void testClimateSetModePayload() throws Exception {
        String payload = HAHttpClient.buildClimateSetMode("stat", "cool");
        JSONObject json = new JSONObject(payload);

        assertEquals("climate", json.getString("domain"));
        assertEquals("stat", json.getString("target"));
        assertEquals("set_mode", json.getString("action"));
        assertEquals("cool", json.getString("mode"));
    }
}
