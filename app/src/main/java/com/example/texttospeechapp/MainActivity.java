package com.example.texttospeechapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.widget.*;
import android.view.View;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.os.Build;
import android.Manifest;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button btnSelectFile, btnPlay, btnStop, btnSaveMp3;
    SeekBar speedControl;
    Spinner languageSpinner;
    TextToSpeech tts;
    float speed = 1.0f;


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

        editText = findViewById(R.id.editText);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnSaveMp3 = findViewById(R.id.btnSaveMp3);
        speedControl = findViewById(R.id.speedControl);
        languageSpinner = findViewById(R.id.languageSpinner);

        // TTS Initialization
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Button actions
        btnSelectFile.setOnClickListener(v -> openFile());
        btnPlay.setOnClickListener(v -> {
            String text = editText.getText().toString();

            final Locale ttsLocale;
            final String targetLangCode;

            int selectedLang = languageSpinner.getSelectedItemPosition();

            switch (selectedLang) {
                case 0:
                    targetLangCode = "en";
                    ttsLocale = Locale.ENGLISH;
                    break;
                case 1:
                    targetLangCode = "fr";
                    ttsLocale = Locale.FRENCH;
                    break;
                case 2:
                    targetLangCode = "de";
                    ttsLocale = Locale.GERMAN;
                    break;
                case 3:
                    targetLangCode = "hi";
                    ttsLocale = new Locale("hi", "IN");
                    break;
                case 4:
                    targetLangCode = "kn";
                    ttsLocale = new Locale("kn", "IN");
                    break;
                default:
                    targetLangCode = "en";
                    ttsLocale = Locale.ENGLISH;
            }

            if (targetLangCode.equals("en")) {
    // English selected — no need to translate
    int result = tts.setLanguage(ttsLocale);
    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Toast.makeText(MainActivity.this, "Selected language is not supported", Toast.LENGTH_SHORT).show();
    } else {
        tts.setSpeechRate(speed);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
} else {
    // Translation needed
    translateText(text, targetLangCode, translated -> {
        if (translated != null) {
            int result = tts.setLanguage(ttsLocale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(MainActivity.this, "Selected language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                tts.setSpeechRate(speed);
                tts.speak(translated, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            Toast.makeText(MainActivity.this, "Translation failed", Toast.LENGTH_SHORT).show();
        }
    });
}

        });


        btnStop.setOnClickListener(v -> tts.stop());
        btnSaveMp3.setOnClickListener(v -> saveAsMp3());

        // Speed control
        speedControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = progress / 50.0f;
                if (speed < 0.1f) speed = 0.1f;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        setupLanguageSpinner();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }

    }

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

    void saveAsMp3() {
        final String text = editText.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "Text is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = "TTS_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".wav";
        File dir = new File(getExternalFilesDir(null), "TTS_Output");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        int result = tts.synthesizeToFile(text, null, file, "ttsOutput");
        if (result == TextToSpeech.SUCCESS) {
            Toast.makeText(this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    void setupLanguageSpinner() {
        List<String> langs = Arrays.asList("English", "French", "German", "Hindi", "Kannada");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langs);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Locale selectedLocale;
                switch (position) {
                    case 0: selectedLocale = Locale.ENGLISH;
                        break;
                    case 1: selectedLocale = Locale.FRENCH;
                        break;
                    case 2: selectedLocale = Locale.GERMAN;
                        break;
                    case 3: selectedLocale = new Locale("hi", "IN");  // Hindi
                        break;
                    case 4: selectedLocale = new Locale("kn", "IN");  // Kannada
                        break;
                    default: selectedLocale = Locale.ENGLISH;
                }

                int availability = tts.isLanguageAvailable(selectedLocale);
                if (availability >= TextToSpeech.LANG_AVAILABLE) {
                    tts.setLanguage(selectedLocale);
                } else {
                    Toast.makeText(MainActivity.this, selectedLocale.getDisplayLanguage() + " not supported on this device", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    // TranslationCallback interface
    interface TranslationCallback {
        void onTranslated(String translatedText);
    }

    // Translation method
    private void translateText(String inputText, String targetLang, TranslationCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://libretranslate.com/translate");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInput = "{\"q\":\"" + inputText + "\",\"source\":\"auto\",\"target\":\"" + targetLang + "\"}";
                OutputStream os = conn.getOutputStream();
                os.write(jsonInput.getBytes());
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                reader.close();
                conn.disconnect();

                // Extract the translated text from response
                String json = result.toString();
                JSONObject jsonObject = new JSONObject(json);
                String translated = jsonObject.getString("translatedText");


                runOnUiThread(() -> callback.onTranslated(translated));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> callback.onTranslated(null));
            }
        }).start();
    }




}
