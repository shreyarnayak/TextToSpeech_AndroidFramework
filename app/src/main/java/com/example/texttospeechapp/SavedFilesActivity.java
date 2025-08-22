package com.example.texttospeechapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class SavedFilesActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<File> audioFiles = new ArrayList<>();
    ArrayList<String> fileNames = new ArrayList<>();
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_files);

        listView = findViewById(R.id.listView);


        // Path to SavedAudios folder
        File dir = new File(getExternalFilesDir(null), "SavedAudios");

        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".wav")) {   // only wav files
                        audioFiles.add(f);
                        fileNames.add(f.getName());
                    }
                }
            }
        }

        if (fileNames.isEmpty()) {
            Toast.makeText(this, "No saved audios found", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, fileNames);
        listView.setAdapter(adapter);

        // On click -> play selected audio
        listView.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = audioFiles.get(position);
            playAudio(selectedFile);
        });
    }

    private void playAudio(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            Toast.makeText(this, "Playing: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
