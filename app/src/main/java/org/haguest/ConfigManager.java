package org.haguest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {
    private static final String PREF_NAME = "haguest_prefs";
    private static final String KEY_WEBHOOK_URL = "webhook_url";
    private static final String KEY_DEVICES_RAW = "devices_raw";

    private final SharedPreferences prefs;

    public ConfigManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class SetupData {
        private final String webhookUrl;
        private final List<Device> devices;

        public SetupData(String webhookUrl, List<Device> devices) {
            this.webhookUrl = webhookUrl != null ? webhookUrl : "";
            this.devices = devices != null ? devices : Collections.<Device>emptyList();
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public List<Device> getDevices() {
            return devices;
        }

        public boolean isValid() {
            return !webhookUrl.isEmpty() && !devices.isEmpty();
        }
    }

    /**
     * Parses a deep link Uri of scheme ha-light://setup?u=<URL>&d=<DEVICES>
     */
    public static SetupData parseUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (!"ha-light".equalsIgnoreCase(scheme)) {
            return null;
        }

        String webhookUrl = uri.getQueryParameter("u");
        String devicesParam = uri.getQueryParameter("d");

        return parseParameters(webhookUrl, devicesParam);
    }

    /**
     * Parses a deep link URL string ha-light://setup?u=<URL>&d=<DEVICES>
     */
    public static SetupData parseUriString(String uriString) {
        if (uriString == null || uriString.trim().isEmpty()) {
            return null;
        }
        try {
            Uri uri = Uri.parse(uriString);
            return parseUri(uri);
        } catch (Exception e) {
            return null;
        }
    }

    public static SetupData parseParameters(String rawWebhookUrl, String rawDevicesParam) {
        if (rawWebhookUrl == null || rawDevicesParam == null) {
            return null;
        }

        String webhookUrl = decode(rawWebhookUrl).trim();
        String devicesStr = decode(rawDevicesParam).trim();

        List<Device> devices = parseDeviceList(devicesStr);
        return new SetupData(webhookUrl, devices);
    }

    public static List<Device> parseDeviceList(String rawDevicesStr) {
        List<Device> result = new ArrayList<>();
        if (rawDevicesStr == null || rawDevicesStr.trim().isEmpty()) {
            return result;
        }

        // Devices are comma-separated: type:id:Label,type:id:Label
        String[] parts = rawDevicesStr.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            String[] deviceFields = part.split(":", 3);
            if (deviceFields.length >= 2) {
                Device.Type type = Device.Type.fromCode(deviceFields[0].trim());
                String id = deviceFields[1].trim();
                String label;
                if (deviceFields.length >= 3) {
                    label = deviceFields[2].replace('+', ' ').trim();
                } else {
                    label = id;
                }
                result.add(new Device(type, id, label));
            }
        }
        return result;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public void saveSetup(SetupData setupData) {
        if (setupData == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_WEBHOOK_URL, setupData.getWebhookUrl());
        editor.putString(KEY_DEVICES_RAW, serializeDevices(setupData.getDevices()));
        editor.apply();
    }

    public SetupData loadSetup() {
        String webhookUrl = prefs.getString(KEY_WEBHOOK_URL, null);
        String devicesRaw = prefs.getString(KEY_DEVICES_RAW, null);

        if (webhookUrl == null || devicesRaw == null) {
            return null;
        }
        List<Device> devices = deserializeDevices(devicesRaw);
        return new SetupData(webhookUrl, devices);
    }

    public void clearSetup() {
        prefs.edit().remove(KEY_WEBHOOK_URL).remove(KEY_DEVICES_RAW).apply();
    }

    public boolean hasSetup() {
        SetupData setup = loadSetup();
        return setup != null && setup.isValid();
    }

    private static String serializeDevices(List<Device> devices) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < devices.size(); i++) {
            Device d = devices.get(i);
            if (i > 0) sb.append(",");
            sb.append(d.getType().getCode()).append(":")
              .append(d.getId()).append(":")
              .append(d.getLabel().replace(' ', '+'));
        }
        return sb.toString();
    }

    private static List<Device> deserializeDevices(String raw) {
        return parseDeviceList(raw);
    }
}
