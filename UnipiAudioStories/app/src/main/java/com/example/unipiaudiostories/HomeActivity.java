package com.example.unipiaudiostories;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLanguage();
        super.onCreate(savedInstanceState);

        String email = getIntent().getStringExtra("email");
        Log.d("HomeActivity", "Λήφθηκε email: " + email);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_home);

        ImageView imageView1 = findViewById(R.id.imageView);
        ImageView imageView2 = findViewById(R.id.imageView4);
        ImageView imageView3 = findViewById(R.id.imageView8);
        ImageView imageView4 = findViewById(R.id.imageView10);
        ImageView imageView5 = findViewById(R.id.imageView11);
        ImageButton logoutButton = findViewById(R.id.logout_button);
        ImageButton settingsButtonBottom = findViewById(R.id.settings_button_bottom);
        ImageButton analyticsButtonBottom = findViewById(R.id.analytics_button);

        imageView1.setOnClickListener(v -> openStory("Little Red Riding Hood", email));
        imageView2.setOnClickListener(v -> openStory("Cinderella", email));
        imageView3.setOnClickListener(v -> openStory("Snow White and the Seven Dwarfs", email));
        imageView4.setOnClickListener(v -> openStory("The Wolf and the Seven Little Goats", email));
        imageView5.setOnClickListener(v -> openStory("Goldilocks and the Three Bears", email));

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });

        settingsButtonBottom.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        analyticsButtonBottom.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });
    }

    //Ανοίγει μια ιστορία με βάση το όνομά της.
    private void openStory(String storyName, String email) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("stories");

        database.orderByChild("name").equalTo(storyName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    AddInitialStoriesActivity.Story story = snapshot.getValue(AddInitialStoriesActivity.Story.class);
                    if (story != null) {
                        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
                        intent.putExtra("title", story.getName());
                        intent.putExtra("text", story.getText());
                        intent.putExtra("imageUrl", story.getImageUrl());
                        intent.putExtra("email", email);
                        intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        intent.putExtra("storyName", storyName);
                        startActivity(intent);
                    }
                }
            } else {
                Toast.makeText(this, "Η ιστορία δεν βρέθηκε!", Toast.LENGTH_SHORT).show();
            }
        });
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

    //Υπολογίζει και ενημερώνει το συνολικό αριθμό ακροάσεων για κάθε ιστορία(αν χρειααστεί)
    private void calculateAndSetTotalListens() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference("stories");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Integer> storyListenCounts = new HashMap<>();

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    DataSnapshot userStoriesSnapshot = userSnapshot.child("stories");
                    for (DataSnapshot storySnapshot : userStoriesSnapshot.getChildren()) {
                        String storyName = storySnapshot.getKey();
                        Integer listens = storySnapshot.getValue(Integer.class);

                        if (storyName != null && listens != null) {
                            storyListenCounts.put(storyName, storyListenCounts.getOrDefault(storyName, 0) + listens);
                        }
                    }
                }
                storiesRef.get().addOnCompleteListener(storiesTask -> {
                    if (storiesTask.isSuccessful() && storiesTask.getResult() != null) {
                        for (DataSnapshot storySnapshot : storiesTask.getResult().getChildren()) {
                            String storyName = storySnapshot.child("name").getValue(String.class);

                            if (storyName != null && storyListenCounts.containsKey(storyName)) {
                                int totalListens = storyListenCounts.get(storyName);
                                storySnapshot.getRef().child("totalListens").setValue(totalListens)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Log.d("Firebase", "Ενημερώθηκαν οι συνολικές ακροάσεις για την ιστορία:" + storyName);
                                            } else {
                                                Log.e("Firebase", "Απέτυχε η ενημέρωση των συνολικών ακροάσεων για την ιστορία:" + storyName);
                                            }
                                        });
                            }
                        }
                    } else {
                        Log.e("Firebase", "Απέτυχε η ανάκτηση των ιστοριών από τη βάση δεδομένων.");
                    }
                });
            } else {
                Log.e("Firebase", "Απέτυχε η ανάκτηση των χρηστών από τη βάση δεδομένων.");
            }
        });
    }

}