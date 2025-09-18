package com.example.unipiaudiostories;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView userTotalListens;
    private TextView globalTotalListens;
    private TextView userFavoriteStory;
    private TextView globalFavoriteStory;
    private LinearLayout userStoryListensContainer;
    private LinearLayout globalStoryListensContainer;

    private DatabaseReference databaseRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        userTotalListens = findViewById(R.id.user_total_listens);
        globalTotalListens = findViewById(R.id.global_total_listens);
        userFavoriteStory = findViewById(R.id.user_favorite_story);
        globalFavoriteStory = findViewById(R.id.global_favorite_story);
        userStoryListensContainer = findViewById(R.id.user_story_listens_container);
        globalStoryListensContainer = findViewById(R.id.global_story_listens_container);

        // Firebase αναφορά
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Φόρτωση στατιστικών
        loadUserStatistics();
        loadGlobalStatistics();
    }


    //Φορτώνει στατιστικά για τον τρέχοντα χρήστη.
    private void loadUserStatistics() {
        DatabaseReference userStoriesRef = databaseRef.child("users").child(currentUserId).child("stories");
        userStoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Integer> userStoryCounts = new HashMap<>();
                int totalListens = 0;
                String favoriteStory = null;
                int maxListens = 0;

                // Εξαγωγή δεδομένων για τον χρήστη
                for (DataSnapshot storySnapshot : dataSnapshot.getChildren()) {
                    String storyName = storySnapshot.getKey();
                    Integer listens = storySnapshot.getValue(Integer.class);

                    if (storyName != null && listens != null) {
                        userStoryCounts.put(storyName, listens);
                        totalListens += listens;

                        if (listens > maxListens) {
                            maxListens = listens;
                            favoriteStory = storyName;
                        }
                    }
                }

                // Ενημέρωση Views
                userTotalListens.setText(String.valueOf(totalListens));
                userFavoriteStory.setText(favoriteStory != null ? favoriteStory : "N/A");

                // Προσθήκη εγγραφών στον πίνακα
                populateContainer(userStoryListensContainer, userStoryCounts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AnalyticsActivity.this, "Απέτυχε η φόρτωση των στατιστικών του χρήστη.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Φορτώνει στατιστικά για όλες τις ιστορίες στη βάση δεδομένων.
    private void loadGlobalStatistics() {
        DatabaseReference storiesRef = databaseRef.child("stories");
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Integer> globalStoryCounts = new HashMap<>();
                int totalListens = 0;
                String popularStory = null;
                int maxListens = 0;

                // Εξαγωγή δεδομένων για όλες τις ιστορίες
                for (DataSnapshot storySnapshot : dataSnapshot.getChildren()) {
                    String storyName = storySnapshot.child("name").getValue(String.class);
                    Integer listens = storySnapshot.child("totalListens").getValue(Integer.class);

                    if (storyName != null && listens != null) {
                        globalStoryCounts.put(storyName, listens);
                        totalListens += listens;

                        if (listens > maxListens) {
                            maxListens = listens;
                            popularStory = storyName;
                        }
                    }
                }

                // Ενημέρωση Views
                globalTotalListens.setText(String.valueOf(totalListens));
                globalFavoriteStory.setText(popularStory != null ? popularStory : "N/A");

                // Προσθήκη εγγραφών στον πίνακα
                populateContainer(globalStoryListensContainer, globalStoryCounts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AnalyticsActivity.this, "Απέτυχε η φόρτωση των παγκόσμιων στατιστικών.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateContainer(LinearLayout container, Map<String, Integer> storyCounts) {
        container.removeAllViews(); // Καθαρισμός του container πριν προσθέσουμε νέα δεδομένα

        for (Map.Entry<String, Integer> entry : storyCounts.entrySet()) {

            // Δημιουργία ενός TextView για το όνομα της ιστορίας
            TextView titleTextView = new TextView(this);
            titleTextView.setText(entry.getKey());
            titleTextView.setTextSize(16);
            titleTextView.setTextColor(getResources().getColor(android.R.color.black));
            titleTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleTextView.setPadding(8, 8, 8, 4);
            container.addView(titleTextView);

            // Δημιουργία ενός TextView για τον αριθμό ακροάσεων
            TextView listensTextView = new TextView(this);
            listensTextView.setText(entry.getValue() + " listens");
            listensTextView.setTextSize(16);
            listensTextView.setTextColor(getResources().getColor(android.R.color.black));
            listensTextView.setPadding(8, 0, 8, 8);
            container.addView(listensTextView);
        }
    }

}