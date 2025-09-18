package com.example.unipiaudiostories;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Switch muteSwitch;
    private Spinner voiceSpinner;
    private SeekBar volumeSeekBar;

    private TextToSpeech textToSpeech;
    private List<String> displayedVoiceNames;
    private List<Voice> voiceObjects;

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_MUTE = "mute";
    private static final String KEY_VOICE = "voice";
    private static final String KEY_VOLUME = "volume";
    private static final String PREF_THEME = "theme";

    private String savedVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applySavedLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton greekButton = findViewById(R.id.greekButton);
        ImageButton englishButton = findViewById(R.id.englishButton);
        ImageButton frenchButton = findViewById(R.id.frenchButton);

        greekButton.setOnClickListener(v -> changeAppLanguage("el"));
        englishButton.setOnClickListener(v -> changeAppLanguage("en"));
        frenchButton.setOnClickListener(v -> changeAppLanguage("fr"));


        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String currentTheme = prefs.getString(PREF_THEME, "System Default");
        String[] themes = getResources().getStringArray(R.array.theme_options);


        muteSwitch = findViewById(R.id.muteSwitch);
        voiceSpinner = findViewById(R.id.voiceSpinner);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        displayedVoiceNames = new ArrayList<>();
        voiceObjects = new ArrayList<>();

        loadSettings();

        initializeTextToSpeech();

        // Διαχείριση αλλαγής ρύθμισης σίγασης
        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_MUTE, isChecked).apply();
        });

        // Διαχείριση αλλαγής έντασης
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putInt(KEY_VOLUME, progress)
                            .apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SettingsActivity.this, "Η ένταση ρυθμίστηκε στο " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        // Διαχείριση επιλογής φωνής
        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < voiceObjects.size()) {
                    Voice selectedVoice = voiceObjects.get(position);
                    String selectedVoiceName = selectedVoice.getName();

                    if (!selectedVoiceName.equals(savedVoice)) {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(KEY_VOICE, selectedVoiceName)
                                .apply();

                        savedVoice = selectedVoiceName;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.setVoice(selectedVoice);
                            textToSpeech.speak("This is a test voice.", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //Αρχικοποιεί το Text-to-Speech και φορτώνει φωνές.
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    int count = 1;

                    for (Voice voice : textToSpeech.getVoices()) {
                        if (voice.getLocale().getLanguage().equals("en")) {
                            voiceObjects.add(voice);
                            displayedVoiceNames.add("Voice " + count);
                            count++;
                        }
                    }

                    if (displayedVoiceNames.isEmpty()) {
                        Toast.makeText(this, "Δεν υπάρχουν διαθέσιμες αγγλικές φωνές.", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, displayedVoiceNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        voiceSpinner.setAdapter(adapter);

                        if (savedVoice != null) {
                            for (int i = 0; i < voiceObjects.size(); i++) {
                                if (voiceObjects.get(i).getName().equals(savedVoice)) {
                                    voiceSpinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Οι φωνές TTS δεν υποστηρίζονται σε αυτήν την έκδοση Android.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Απέτυχε η αρχικοποίηση της λειτουργίας TTS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Φορτώνει αποθηκευμένες ρυθμίσεις.
    private void loadSettings() {
        savedVoice = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_VOICE, null);
        boolean isMuted = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_MUTE, false);
        int volume = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_VOLUME, 50);

        muteSwitch.setChecked(isMuted);
        volumeSeekBar.setProgress(volume);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    //Αλλάζει τη γλώσσα της εφαρμογής.
    private void changeAppLanguage(String languageCode) {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        prefs.edit().putString("language", languageCode).apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Εφαρμόζει τη γλώσσα που έχει αποθηκευτεί
    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String languageCode = prefs.getString("language", "en");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
