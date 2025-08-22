package com.example.texttospeechapp;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button btnSelectFile, btnPlay, btnStop, btnSaveMp3, btnOpenSaved, btnSavedFiles;
    SeekBar speedControl;
    Spinner languageSpinner;
    TextToSpeech tts;
    float speed = 1.0f;

    String selectedLanguageCode = "en"; // default English
    HashMap<String, String> langMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        editText = findViewById(R.id.editText);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnSaveMp3 = findViewById(R.id.btnSaveMp3);
        //btnOpenSaved = findViewById(R.id.btnOpenSaved)
        speedControl = findViewById(R.id.speedControl);
        btnSavedFiles = findViewById(R.id.btnSavedFiles);
        languageSpinner = findViewById(R.id.languageSpinner);

        // TTS Initialization
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Button actions
        btnSelectFile.setOnClickListener(v -> openFile());
        btnPlay.setOnClickListener(v -> playText());
        btnStop.setOnClickListener(v -> tts.stop());
        btnSaveMp3.setOnClickListener(v -> saveAsMp3());

        // ✅ Open Saved Files screen
        btnSavedFiles.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedFilesActivity.class);
            startActivity(intent);
        });

        // Speed control
        speedControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = progress / 50.0f;
                if (speed < 0.1f) speed = 0.1f;
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        setupLanguageSpinner();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    // --- File selection ---
    void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                editText.setText(sb.toString());
            } catch (Exception e) {
                Toast.makeText(this, "File read error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Save as MP3 ---
    void saveAsMp3() {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "Text is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "TTS_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".wav";

        // Custom folder inside device storage
        File dir = new File(getExternalFilesDir(null), "SavedAudios");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        int result = tts.synthesizeToFile(text, null, file, "ttsOutput");
        if (result == TextToSpeech.SUCCESS) {
            Toast.makeText(this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Setup language dropdown ---
    void setupLanguageSpinner() {
        langMap = new HashMap<>();
        langMap.put("English", "en");
        langMap.put("Hindi", "hi");
        langMap.put("Kannada", "kn");
        langMap.put("French", "fr");
        langMap.put("Spanish", "es");

        List<String> languages = new ArrayList<>(langMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = languages.get(position);
                selectedLanguageCode = langMap.get(selected);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedLanguageCode = "en";
            }
        });
    }

    // --- Play text with translation if needed ---
    void playText() {
        String input = editText.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Enter some text", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLanguageCode.equals("en")) {
            // Directly speak in English
            tts.setSpeechRate(speed);
            tts.speak(input, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            // Translate first, then speak
            new Thread(() -> {
                try {
                    String translated = translateText(input, "en", selectedLanguageCode);
                    runOnUiThread(() -> {
                        tts.setSpeechRate(speed);
                        tts.speak(translated, TextToSpeech.QUEUE_FLUSH, null, null);
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Translation failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        }
    }

    // --- Translation using LibreTranslate ---
    String translateText(String inputText, String sourceLang, String targetLang) throws Exception {
        URL url = new URL("https://libretranslate.de/translate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String data = "q=" + URLEncoder.encode(inputText, "UTF-8") +
                "&source=" + URLEncoder.encode(sourceLang, "UTF-8") +
                "&target=" + URLEncoder.encode(targetLang, "UTF-8") +
                "&format=" + URLEncoder.encode("text", "UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
            os.flush();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject json = new JSONObject(response.toString());
        return json.getString("translatedText");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
