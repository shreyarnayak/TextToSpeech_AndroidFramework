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


   private void translateText(String inputText, String targetLang, TranslationCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://libretranslate.de/translate");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("q", inputText);
                body.put("source", "auto");
                body.put("target", targetLang);
                body.put("format", "text");

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("HTTP error: " + responseCode);
                    runOnUiThread(() -> callback.onTranslated(null));
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                reader.close();
                conn.disconnect();

                JSONObject jsonObject = new JSONObject(result.toString());
                String translated = jsonObject.getString("translatedText");

                runOnUiThread(() -> callback.onTranslated(translated));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> callback.onTranslated(null));
            }
        }).start();
    }


}
