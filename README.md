# haguest
Local Guest Webhook Controller (Lights & Thermostats)An ultra-lightweight, zero-auth, high-speed local controller for Home Assistant lights and thermostats. Built specifically for sub-second response times, ephemeral guest access, and an ultra-minimal Android binary footprint (<150 KB).Key FeaturesExtreme Performance: Cold starts in under 60 ms; local LAN execution latency under 25 ms.Microscopic APK Footprint: Pure native Android primitives without third-party frameworks, Compose, or external networking libraries.Ephemeral Guest Access: Onboard visitors instantly via QR code or deep link with zero account creation, cloud dependencies, or persistent token exposure.Granular Control:Lights: Instant toggle, hard off, and multi-tier brightness presets (Low 15%, Medium 50%, High 100%).Climate: Incremental temperature stepping ($\pm 1^\circ$), direct setpoint assignment, and HVAC mode switching (heat, cool, auto, off).Seamless Handoff: Includes an "Advanced" launcher that deep links directly into the official Home Assistant companion app and self-terminates.Architecture Overview+-----------------------------------------------------------+
|               Host Home Assistant Instance                |
|  - Imports "Local Guest Webhook Controller" Blueprint     |
|  - Binds endpoint: /api/webhook/<webhook_id>              |
|  - (Optional) Sets expiry via input_datetime helper       |
+-----------------------------+-----------------------------+
                              |
                     [Scans QR / Deep Link]
                              |
                              v
+-----------------------------------------------------------+
|               Lightweight Android Client                  |
|  - Receives setup via ha-light:// URI                     |
|  - Stores webhook URL & device list in SharedPreferences  |
|  - Renders UI dynamically: Light rows & Climate cards     |
+-----------------------------+-----------------------------+
                              |
             [User Taps Toggle / Stepper Control]
                              |
                              v
+-----------------------------------------------------------+
|           Direct Raw HTTP POST over Local LAN             |
|  - HttpURLConnection (500 ms connect/read timeout)        |
|  - Direct dispatch to HA event bus (local_only: true)     |
+-----------------------------------------------------------+
Home Assistant Setup1. Import the BlueprintCopy the raw content of guest_light_webhook.yaml into your Home Assistant configuration directory under:
/config/blueprints/automation/guest_light_webhook.yamlOr import directly using your local Home Assistant web interface (Settings > Automations & Scenes > Blueprints > Import Blueprint).2. Configure the AutomationCreate an automation from the imported blueprint and configure the inputs:Webhook ID: Set a unique or secret string (e.g., guest_ctrl_91a8df).Allowed Lights: Pick the light entities accessible to this controller.Allowed Thermostats: Pick the climate entities accessible to this controller.(Optional) Access Expiration Timestamp: Assign an input_datetime entity. Requests received after this timestamp will be rejected automatically.Onboarding Protocol & Deep Link FormatThe Android client is provisioned via the custom URI scheme ha-light://setup.URI Scheme Specificationha-light://setup?u=<WEBHOOK_URL>&d=<DEVICE_LIST>
ParameterDescriptionExampleuFull local webhook target URLhttp://192.168.1.100:8123/api/webhook/guest_ctrl_91a8dfdComma-delimited list of typed devices (type:id:Label)l:desk:Desk+Lamp,l:bed:Guest+Bed,c:stat:ThermostatDevice Type Identifiersl — Light: Generates a row with a primary Toggle button and preset chips (L, M, H).c — Climate / Thermostat: Generates a card with stepper buttons (-1°, +1°) and quick HVAC mode controls (Cool, Heat, Off).Generating a Setup QR CodeEncode the target URI into any standard QR code generator (or generate it directly inside Home Assistant using a Markdown or Webpage card):ha-light://setup?u=http%3A%2F%2F192.168.1.100%3A8123%2Fapi%2Fwebhook%2Fguest_ctrl_91a8df&d=l%3Adesk%3ADesk%2Bl%3Abed%3AGuest%2Bc%3Astat%3AThermostat
Guests scan the QR code using their phone's native camera to launch the onboarding handler.Network API ContractsAll commands are dispatched as lightweight JSON payloads over POST:1. Light Domain{
  "domain": "light",
  "target": "desk",
  "action": "toggle"
}
Actions: toggle, off, preset, onLevels (optional): low, medium, high2. Climate Domain{
  "domain": "climate",
  "target": "stat",
  "action": "temp_delta",
  "value": 1.0
}
Actions:temp_delta: Relative adjustment (e.g., value: 1 or value: -1).set_temp: Absolute temperature setting (e.g., value: 72).set_mode: HVAC mode selection (mode: "cool" | "heat" | "auto" | "off").Repository Filesroadmap.md: Project delivery roadmap, technical budgets, and architectural constraints.guest_light_webhook.yaml: Home Assistant Community Blueprint handling multi-domain webhook routing and expiration verification.README.md: System documentation, payload schemas, and onboarding specifications.
