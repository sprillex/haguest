package org.haguest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private ConfigManager configManager;
    private HAHttpClient httpClient;

    private LinearLayout contentContainer;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configManager = new ConfigManager(this);
        httpClient = new HAHttpClient();

        handleIntent(getIntent());
        buildUi();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        buildUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpClient != null) {
            httpClient.shutdown();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null && "ha-light".equalsIgnoreCase(data.getScheme())) {
            ConfigManager.SetupData setup = ConfigManager.parseUri(data);
            if (setup != null && setup.isValid()) {
                configManager.saveSetup(setup);
                Toast.makeText(this, getString(R.string.setup_success, setup.getDevices().size()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int dp(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    private int getColorCompat(int resId) {
        return getResources().getColor(resId, getTheme());
    }

    private GradientDrawable makeRoundedDrawable(int bgColor, int strokeColor, int strokeWidthDp, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(radiusDp));
        if (strokeWidthDp > 0) {
            gd.setStroke(dp(strokeWidthDp), strokeColor);
        }
        return gd;
    }

    private void buildUi() {
        int pad = dp(16);

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColorCompat(R.color.bg_color));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(pad, dp(12), pad, dp(12));

        TextView titleView = new TextView(this);
        titleView.setText(R.string.title_devices);
        titleView.setTextColor(getColorCompat(R.color.text_primary));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(titleView, titleLp);

        // Advanced Launcher Button
        Button advBtn = createStyledButton(getString(R.string.action_advanced), false);
        advBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        advBtn.setPadding(dp(10), dp(6), dp(10), dp(6));
        advBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAdvancedAndExit();
            }
        });
        topBar.addView(advBtn);

        root.addView(topBar);

        // Status banner
        statusTextView = new TextView(this);
        statusTextView.setVisibility(View.GONE);
        statusTextView.setPadding(pad, dp(6), pad, dp(6));
        statusTextView.setGravity(Gravity.CENTER);
        statusTextView.setTextColor(getColorCompat(R.color.status_text));
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        statusTextView.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(statusTextView);

        // Scrollable content
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        );
        scrollView.setLayoutParams(scrollLp);

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(pad, dp(8), pad, pad);
        scrollView.addView(contentContainer);

        root.addView(scrollView);

        // Populate content
        renderContent();

        setContentView(root);
    }

    private void renderContent() {
        contentContainer.removeAllViews();

        ConfigManager.SetupData setup = configManager.loadSetup();
        if (setup == null || !setup.isValid()) {
            renderEmptyState();
        } else {
            renderDeviceList(setup);
        }
    }

    private void renderEmptyState() {
        LinearLayout emptyLayout = new LinearLayout(this);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(Gravity.CENTER);
        emptyLayout.setPadding(dp(24), dp(48), dp(24), dp(48));

        TextView emptyTitle = new TextView(this);
        emptyTitle.setText(R.string.no_config_title);
        emptyTitle.setTextColor(getColorCompat(R.color.text_primary));
        emptyTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyTitle.setTypeface(Typeface.DEFAULT_BOLD);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyLayout.addView(emptyTitle);

        TextView emptyDesc = new TextView(this);
        emptyDesc.setText(R.string.no_config_desc);
        emptyDesc.setTextColor(getColorCompat(R.color.text_secondary));
        emptyDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        emptyDesc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descLp.topMargin = dp(12);
        emptyLayout.addView(emptyDesc, descLp);

        contentContainer.addView(emptyLayout);
    }

    private void renderDeviceList(final ConfigManager.SetupData setup) {
        List<Device> devices = setup.getDevices();

        for (final Device device : devices) {
            if (device.getType() == Device.Type.LIGHT) {
                contentContainer.addView(createLightRow(setup.getWebhookUrl(), device));
            } else if (device.getType() == Device.Type.CLIMATE) {
                contentContainer.addView(createClimateCard(setup.getWebhookUrl(), device));
            }
        }

        // Reset configuration button at the bottom
        Button resetBtn = createStyledButton(getString(R.string.action_clear), false);
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        resetLp.topMargin = dp(24);
        resetLp.bottomMargin = dp(16);
        resetBtn.setLayoutParams(resetLp);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configManager.clearSetup();
                renderContent();
            }
        });
        contentContainer.addView(resetBtn);
    }

    private View createLightRow(final String webhookUrl, final Device device) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeRoundedDrawable(
                getColorCompat(R.color.card_bg),
                getColorCompat(R.color.card_stroke),
                1, 12
        ));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardLp.bottomMargin = dp(12);
        card.setLayoutParams(cardLp);

        // Header: Device label
        TextView nameView = new TextView(this);
        nameView.setText(device.getLabel());
        nameView.setTextColor(getColorCompat(R.color.text_primary));
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(nameView);

        // Controls container
        LinearLayout controlsRow = new LinearLayout(this);
        controlsRow.setOrientation(LinearLayout.HORIZONTAL);
        controlsRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams controlsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        controlsLp.topMargin = dp(10);
        controlsRow.setLayoutParams(controlsLp);

        // Toggle button (Primary accent)
        Button toggleBtn = createStyledButton(getString(R.string.btn_toggle), true);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand(webhookUrl, HAHttpClient.buildLightToggle(device.getId()));
            }
        });
        controlsRow.addView(toggleBtn);

        // Off button
        Button offBtn = createStyledButton(getString(R.string.btn_off), false);
        LinearLayout.LayoutParams offLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        offLp.leftMargin = dp(8);
        offBtn.setLayoutParams(offLp);
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand(webhookUrl, HAHttpClient.buildLightOff(device.getId()));
            }
        });
        controlsRow.addView(offBtn);

        // Brightness Presets: Low, Med, High
        String[] presets = new String[]{"low", "medium", "high"};
        String[] presetLabels = new String[]{
                getString(R.string.btn_low),
                getString(R.string.btn_med),
                getString(R.string.btn_high)
        };

        for (int i = 0; i < presets.length; i++) {
            final String lvl = presets[i];
            Button chip = createStyledButton(presetLabels[i], false);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            chipLp.leftMargin = dp(6);
            chip.setLayoutParams(chipLp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchCommand(webhookUrl, HAHttpClient.buildLightPreset(device.getId(), lvl));
                }
            });
            controlsRow.addView(chip);
        }

        // Horizontal scroll to accommodate small screens
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.addView(controlsRow);
        card.addView(hsv);

        return card;
    }

    private View createClimateCard(final String webhookUrl, final Device device) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeRoundedDrawable(
                getColorCompat(R.color.card_bg),
                getColorCompat(R.color.card_stroke),
                1, 12
        ));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardLp.bottomMargin = dp(12);
        card.setLayoutParams(cardLp);

        // Header: Device label
        TextView nameView = new TextView(this);
        nameView.setText(device.getLabel());
        nameView.setTextColor(getColorCompat(R.color.text_primary));
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(nameView);

        // Stepper row: -1°, +1°
        LinearLayout stepperRow = new LinearLayout(this);
        stepperRow.setOrientation(LinearLayout.HORIZONTAL);
        stepperRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stepperLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        stepperLp.topMargin = dp(10);
        stepperRow.setLayoutParams(stepperLp);

        Button minusBtn = createStyledButton(getString(R.string.btn_step_down), false);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand(webhookUrl, HAHttpClient.buildClimateTempDelta(device.getId(), -1.0));
            }
        });
        stepperRow.addView(minusBtn);

        Button plusBtn = createStyledButton(getString(R.string.btn_step_up), false);
        LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        plusLp.leftMargin = dp(8);
        plusBtn.setLayoutParams(plusLp);
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand(webhookUrl, HAHttpClient.buildClimateTempDelta(device.getId(), 1.0));
            }
        });
        stepperRow.addView(plusBtn);

        card.addView(stepperRow);

        // Mode row: Cool, Heat, Auto, Off
        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams modeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        modeLp.topMargin = dp(10);
        modeRow.setLayoutParams(modeLp);

        String[] modes = new String[]{"cool", "heat", "auto", "off"};
        String[] modeLabels = new String[]{
                getString(R.string.btn_cool),
                getString(R.string.btn_heat),
                getString(R.string.btn_auto),
                getString(R.string.btn_off)
        };

        for (int i = 0; i < modes.length; i++) {
            final String m = modes[i];
            Button modeBtn = createStyledButton(modeLabels[i], false);
            modeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            if (i > 0) {
                LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
                mLp.leftMargin = dp(6);
                modeBtn.setLayoutParams(mLp);
            }
            modeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchCommand(webhookUrl, HAHttpClient.buildClimateSetMode(device.getId(), m));
                }
            });
            modeRow.addView(modeBtn);
        }

        HorizontalScrollView modeScroll = new HorizontalScrollView(this);
        modeScroll.setHorizontalScrollBarEnabled(false);
        modeScroll.addView(modeRow);
        card.addView(modeScroll);

        return card;
    }

    private Button createStyledButton(String text, boolean isAccent) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setMinHeight(dp(36));
        btn.setMinimumHeight(dp(36));
        btn.setPadding(dp(14), dp(6), dp(14), dp(6));

        int bgColor = isAccent ? getColorCompat(R.color.btn_accent_bg) : getColorCompat(R.color.btn_bg);
        int textColor = isAccent ? getColorCompat(R.color.btn_accent_text) : getColorCompat(R.color.btn_text);

        btn.setTextColor(textColor);
        btn.setBackground(makeRoundedDrawable(bgColor, 0, 0, 8));
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        return btn;
    }

    private void dispatchCommand(String webhookUrl, String payload) {
        showStatus(getString(R.string.status_dispatched));
        httpClient.post(webhookUrl, payload, new HAHttpClient.Callback() {
            @Override
            public void onSuccess(int statusCode) {
                if (statusCode >= 200 && statusCode < 300) {
                    showStatus(getString(R.string.status_dispatched));
                } else {
                    showStatus(getString(R.string.status_failed, "HTTP " + statusCode));
                }
            }

            @Override
            public void onError(Exception error) {
                showStatus(getString(R.string.status_failed, error.getMessage()));
            }
        });
    }

    private void showStatus(final String message) {
        if (statusTextView == null) return;
        statusTextView.setText(message);
        statusTextView.setVisibility(View.VISIBLE);
        statusTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (statusTextView != null) {
                    statusTextView.setVisibility(View.GONE);
                }
            }
        }, 1500);
    }

    private void launchAdvancedAndExit() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("io.homeassistant.companion.android");
        if (intent == null) {
            // Fallback to URI scheme or Play Store
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("homeassistant://"));
            if (getPackageManager().resolveActivity(intent, 0) == null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.homeassistant.companion.android"));
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception ignored) {
        }
        finish();
    }
}
