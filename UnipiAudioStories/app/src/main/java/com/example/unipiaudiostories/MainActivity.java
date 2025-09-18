package com.example.unipiaudiostories;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applySavedLanguage();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("userAnalytics");

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button signInButton = findViewById(R.id.sign_in_button);
        Button signUpButton = findViewById(R.id.sign_up_button);
        ImageView passwordI = findViewById(R.id.password_icon);

        final boolean[] isPasswordVisible = {false};

        passwordI.setOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];
            if (isPasswordVisible[0]) {
                passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Διαχείριση Sign In
        signInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                updateUI(user);
                            } else {
                                Toast.makeText(MainActivity.this, R.string.error_sign_in, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });

        // Διαχείριση Sign Up
        signUpButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (password.length() < 6) {
                    Toast.makeText(MainActivity.this, "Ο κωδικός πρέπει να αποτελείται από τουλάχιστον 6 χαρακτήρες.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Toast.makeText(MainActivity.this, "Η εγγραφή ήταν επιτυχής! Καλώς ήρθατε, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    UserAnalytics userAnalytics = new UserAnalytics(user.getEmail(), "", 0);
                                    String userId = user.getUid(); // Unique ID for the user
                                    databaseRef.child(userId).setValue(userAnalytics).addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Τα δεδομένα καταγράφηκαν με επιτυχία.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Απέτυχε η καταγραφή των δεδομένων.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    updateUI(user);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Η εγγραφή απέτυχε: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Παρακαλώ συμπληρώστε όλα τα πεδία.", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> signInWithGoogle());
    }

    //Διαχειρίζεται το Sign-In μέσω Google.
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                Log.e("GoogleSignIn", "Sign-in απέτυχε: " + e.getLocalizedMessage());
            }
        }
    }

    //Αυθεντικοποιεί τον χρήστη μέσω Google.
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w("FirebaseAuth", "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();

            Log.d("updateUI", "User ID: " + userId);
            Log.d("updateUI", "User Email: " + email);

            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

            databaseRef.child(userId).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("updateUI", "Απέτυχε η επιβεβαίωση ύπαρξης του χρήστη: ", task.getException());
                    return;
                }

                if (task.getResult() == null || !task.getResult().exists()) {
                    Log.d("updateUI", "Ο χρήστης δεν υπάρχει. Δημιουργία νέας εγγραφής.");

                    Map<String, Object> userRecord = new HashMap<>();
                    userRecord.put("email", email);

                    Map<String, Integer> stories = new HashMap<>();
                    stories.put("Little Red Riding Hood", 0);
                    stories.put("Cinderella", 0);
                    stories.put("Snow White and the Seven Dwarfs", 0);
                    stories.put("The Wolf and the Seven Little Goats", 0);
                    stories.put("Goldilocks and the Three Bears", 0);

                    userRecord.put("stories", stories);

                    Log.d("updateUI", "Η εγγραφή του χρήστη που θα αποθηκευτεί: " + userRecord);

                    databaseRef.child(userId).setValue(userRecord).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d("updateUI", "Η εγγραφή του χρήστη δημιουργήθηκε με επιτυχία.");
                        } else {
                            Log.e("updateUI", "Απέτυχε η δημιουργία της εγγραφής του χρήστη:", task1.getException());
                        }
                    });
                } else {
                    Log.d("updateUI", "Ο χρήστης υπάρχει ήδη. Παράλειψη δημιουργίας.");
                }
            });

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        } else {
            Log.d("updateUI", "Κανένας χρήστης δεν έχει συνδεθεί.");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("email", currentUser.getEmail());
            startActivity(intent);
            finish();
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

    //Κλάση για αναλυτικά δεδομένα χρήστη.
    class UserAnalytics {
        public String email;
        public String favorityStory;
        public int totalListenedStories;

        public UserAnalytics(String email, String favorityStory, int totalListenedStories) {
            this.email = email;
            this.favorityStory = favorityStory;
            this.totalListenedStories = totalListenedStories;
        }
    }

}