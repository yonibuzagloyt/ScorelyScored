package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.firebase.FirebaseNetworkException;

public class MainActivity extends AppCompatActivity {
    private static final String INFO = "login_info";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    public static final String DATABASE_URL = "https://scorely-475cf-default-rtdb.europe-west1.firebasedatabase.app";
    public  static final String CHANNEL_ID   = "demo_channel";
    private static final int    NOTIF_ID     = 101;
    private static final int    PERM_REQUEST = 1001;

    private EditText etEmail, etPassword, etRepeatPassword, etUsername;
    private SharedPreferences sp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(INFO, MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        createNotificationChannel();
        askNotificationPermission();
        showLogin();
    }
    /** ערוץ ההתראות. מ-API 26 התראה בלי ערוץ פשוט לא תוצג. */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "הודעות כלליות",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("התראות של אפליקציית ההדגמה");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    /** מ-API 33 (TIRAMISU) חייבים לבקש מהמשתמש הרשאה להצגת התראות. */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERM_REQUEST);
            }
        }
    }
    private void showLogin() {
        setContentView(R.layout.activity_main);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etEmail.setText(sp.getString(KEY_EMAIL, ""));
        etPassword.setText(sp.getString(KEY_PASSWORD, ""));

        Button btnLogin = findViewById(R.id.btnLogIn);
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_in_email_and_password, Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(this, R.string.please_put_in_an_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_in_a_password, Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveLogin(email, password);   // optional: remember the last login
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseNetworkException) {
                                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                            } else {
                                // wrong password or unknown email
                                Toast.makeText(this, R.string.wrong_email_or_password, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        Button btnForgot = findViewById(R.id.btnForgotPassword);
        btnForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, R.string.please_put_in_an_email, Toast.LENGTH_LONG).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // the saved password is now outdated, so forget it
                            sp.edit().remove(KEY_PASSWORD).apply();
                            showNotification();
                        } else if (task.getException() instanceof FirebaseNetworkException) {
                            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.couldn_t_send_the_reset_email_check_the_address, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        Button btnSignUp = findViewById(R.id.btnCreateAccount);
        btnSignUp.setOnClickListener(v -> showRegister());
    }

    private void showRegister() {
        setContentView(R.layout.activity_register);
        etUsername = findViewById(R.id.etUserName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();

            if (username.isEmpty()) {
                Toast.makeText(this, R.string.please_put_in_an_username, Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(this, R.string.please_put_in_an_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_in_a_password, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(repeatPassword)) {
                Toast.makeText(this, R.string.passwords_match, Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, R.string.password_must_be_at_least_6_characters, Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            assert auth.getCurrentUser() != null;
                            String uid = auth.getCurrentUser().getUid();

                            Map<String, Object> data = new HashMap<>();
                            data.put("username", username);
                            data.put("email", email);
                            FirebaseDatabase.getInstance(DATABASE_URL)
                                    .getReference("users").child(uid).setValue(data);

                            saveLogin(email, password);   // SharedPreferences, only after success
                            showLogin();                  // fresh screen, listeners attached, fields prefilled
                        } else {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void saveLogin(String email, String pass) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, pass);
        editor.apply();
    }

    private void forgetLogin() {
        sp.edit().clear().apply();
        auth.signOut();   // for signing out on activity_home
    }
    private void showNotification() {
        // לאן לקפוץ כשלוחצים על ההתראה
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);

        // "מעטפה מבוילת לתשובה" — המערכת תפעיל את ה-Intent בשמנו
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_email)
                            .setContentTitle("Reset Password")
                        .setContentText(getText(R.string.if_this_email_is_registered_a_reset_link_was_sent_check_your_spam_folder_too))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(""))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pi)
                        .setAutoCancel(true);    // ההתראה תיעלם אחרי הלחיצה

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIF_ID, builder.build());
        }
    }

    private void cancelNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIF_ID);
    }
}