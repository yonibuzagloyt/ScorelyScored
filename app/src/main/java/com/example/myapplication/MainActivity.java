package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

    private EditText etEmail, etPassword, etRepeatPassword, etUsername;
    private SharedPreferences sp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(INFO, MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        showLogin();
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
                            // startActivity(new Intent(MainActivity.this, HomeActivity.class));
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
                Toast.makeText(this, R.string.type_your_email_first_then_press_forgot_password, Toast.LENGTH_LONG).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // the saved password is now outdated, so forget it
                            sp.edit().remove(KEY_PASSWORD).apply();
                            Toast.makeText(this, R.string.if_this_email_is_registered_a_reset_link_was_sent_check_your_spam_folder_too, Toast.LENGTH_LONG).show();
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
}