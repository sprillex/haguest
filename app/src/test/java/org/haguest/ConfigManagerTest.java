package org.haguest;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigManagerTest {

    @Test
    public void testParseDeviceList() {
        String input = "l:desk:Desk+Lamp,l:bed:Guest+Bed,c:stat:Thermostat";
        List<Device> devices = ConfigManager.parseDeviceList(input);

        assertEquals(3, devices.size());

        assertEquals(Device.Type.LIGHT, devices.get(0).getType());
        assertEquals("desk", devices.get(0).getId());
        assertEquals("Desk Lamp", devices.get(0).getLabel());

        assertEquals(Device.Type.LIGHT, devices.get(1).getType());
        assertEquals("bed", devices.get(1).getId());
        assertEquals("Guest Bed", devices.get(1).getLabel());

        assertEquals(Device.Type.CLIMATE, devices.get(2).getType());
        assertEquals("stat", devices.get(2).getId());
        assertEquals("Thermostat", devices.get(2).getLabel());
    }

    @Test
    public void testParseParametersUrlEncoded() {
        String webhookUrl = "http%3A%2F%2F192.168.1.100%3A8123%2Fapi%2Fwebhook%2Fguest_ctrl_91a8df";
        String devices = "l%3Adesk%3ADesk%2BLamp%2Cc%3Astat%3AHouse%2BThermostat";

        ConfigManager.SetupData data = ConfigManager.parseParameters(webhookUrl, devices);
        assertNotNull(data);
        assertEquals("http://192.168.1.100:8123/api/webhook/guest_ctrl_91a8df", data.getWebhookUrl());
        assertEquals(2, data.getDevices().size());
        assertEquals("Desk Lamp", data.getDevices().get(0).getLabel());
        assertEquals("House Thermostat", data.getDevices().get(1).getLabel());
        assertTrue(data.isValid());
    }

    @Test
    public void testParseInvalidParameters() {
        assertNull(ConfigManager.parseParameters(null, "l:1:Test"));
        assertNull(ConfigManager.parseParameters("http://localhost", null));
    }
}
