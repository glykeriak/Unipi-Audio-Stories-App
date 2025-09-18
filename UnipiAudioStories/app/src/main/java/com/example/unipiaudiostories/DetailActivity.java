package com.example.unipiaudiostories;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private SharedPreferences sharedPreferences;
    private boolean isMuted;
    private int volume;
    private String selectedVoiceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applySavedLanguage();

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_detail);

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        loadSettings();

        initializeTextToSpeech();

        TextView storyTitle = findViewById(R.id.storyTitle);
        TextView storyText = findViewById(R.id.storyText);
        ImageView storyImage = findViewById(R.id.storyImage);
        ImageButton playButton = findViewById(R.id.playButton);

        String title = getIntent().getStringExtra("title");
        String text = getIntent().getStringExtra("text");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String email = getIntent().getStringExtra("email");
        String storyName = getIntent().getStringExtra("storyName");

        storyTitle.setText(title);
        storyText.setText(text);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(storyImage);
        } else {
            storyImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Διαχείριση του κουμπιού αναπαραγωγής
        playButton.setOnClickListener(v -> {
            if (textToSpeech == null) {
                Log.e("TTS", "Το TTS δεν έχει αρχικοποιηθεί.");
                Toast.makeText(this, "Το Text-to-Speech δεν είναι διαθέσιμο.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isMuted) {
                Log.d("TTS", "Το TTS είναι σε σίγαση.");
                Toast.makeText(this, "Το TTS είναι σε σίγαση. Απενεργοποιήστε το mute στις ρυθμίσεις.", Toast.LENGTH_SHORT).show();
                return;
            }

            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.setPitch(1.0f);

            // Ρύθμιση της έντασης ήχου
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (volume > maxVolume) {
                    volume = maxVolume;
                    Log.d("DetailActivity", "Volume υπερβαίνει το μέγιστο. Ορίστηκε στο: " + maxVolume);
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            }
            // Αύξηση του μετρητή ακροάσεων
            incrementStoryCount(FirebaseAuth.getInstance().getCurrentUser().getUid(), storyName);

            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    //Φορτώνει τις ρυθμίσεις του χρήστη από τα SharedPreferences.
    private void loadSettings() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        }

        isMuted = sharedPreferences.getBoolean("mute", false);
        volume = sharedPreferences.getInt("volume", 50);
        selectedVoiceName = sharedPreferences.getString("voice", null);
        Log.d("DetailActivity", "Ρυθμίσεις φορτώθηκαν: Mute=" + isMuted + ", Volume=" + volume + ", Voice=" + selectedVoiceName);
    }

    //Αρχικοποιεί το Text-to-Speech και ρυθμίζει τη φωνή.
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setPreferredVoice();
                }
            } else {
                Toast.makeText(this, "Απέτυχε η αρχικοποίηση της λειτουργίας TTS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Ρυθμίζει τη φωνή που προτιμάται από τον χρήστη.
    private void setPreferredVoice() {
        if (selectedVoiceName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Voice voice : textToSpeech.getVoices()) {
                if (voice.getName().equals(selectedVoiceName)) {
                    textToSpeech.setVoice(voice);
                    Log.d("DetailActivity", "Επιλέχθηκε φωνή: " + selectedVoiceName);
                    return;
                }
            }
            Log.w("DetailActivity", "Η επιλεγμένη φωνή δεν βρέθηκε.");
        }
    }

    //Εφαρμόζει τη γλώσσα που έχει αποθηκευτεί στις ρυθμίσεις.
    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String languageCode = prefs.getString("language", "en");

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    //Αυξάνει τον αριθμό ακροάσεων μιας ιστορίας τόσο για τον χρήστη όσο και συνολικά.
    private void incrementStoryCount(String userId, String storyName) {
        DatabaseReference userStoryRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("stories").child(storyName);
        DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference("stories");

        userStoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer currentCount = task.getResult().getValue(Integer.class);
                if (currentCount != null) {
                    userStoryRef.setValue(currentCount + 1).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d("Firebase", "Η καταμέτρηση των ιστοριών χρήστη ενημερώθηκε με επιτυχία.");
                        } else {
                            Log.e("Firebase", "Απέτυχε η ενημέρωση της καταμέτρησης των ιστοριών χρήστη.");
                        }
                    });
                }
            }
        });

        storiesRef.orderByChild("name").equalTo(storyName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot storySnapshot : task.getResult().getChildren()) {
                    DatabaseReference storyTotalListensRef = storySnapshot.getRef().child("totalListens");
                    storyTotalListensRef.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Integer currentTotalListens = task1.getResult().getValue(Integer.class);
                            if (currentTotalListens != null) {
                                storyTotalListensRef.setValue(currentTotalListens + 1).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Log.d("Firebase", "Οι συνολικές ακροάσεις της ιστορίας ενημερώθηκαν με επιτυχία");
                                    } else {
                                        Log.e("Firebase", "Απέτυχε η ενημέρωση των συνολικών ακροάσεων της ιστορίας.");
                                    }
                                });
                            }
                        }
                    });
                }
            } else {
                Log.e("Firebase", "Απέτυχε η εύρεση της ιστορίας στη βάση δεδομένων.");
            }
        });
    }
}