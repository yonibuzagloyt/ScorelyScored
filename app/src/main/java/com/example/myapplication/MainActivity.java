package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String INFO = "login_info";
    private static final String KEY_EMAIL  = "email";
    private static final String KEY_PASSWORD  = "password";
    public static final String API_KEY = "AIzaSyD3-unNoqY9ohiomlR7-4LgbPaCjiGmF1U";
    public static final String APPLICATION_ID = "1:45465032872:android:433f9edafbf2a573614a9d";
    public static final String DATABASE_URL = "https://scorely-475cf-default-rtdb.europe-west1.firebasedatabase.app";
    private EditText etEmail, etPassword, etRepeatPassword, etUsername;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        // 1. משיגים את קובץ ההעדפות
        sp = getSharedPreferences(INFO, MODE_PRIVATE);
        etEmail.setText(sp.getString(KEY_EMAIL, ""));
        etPassword.setText(sp.getString(KEY_PASSWORD, ""));
        Button btnLogin = findViewById(R.id.btnLogIn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // תעביר אותנו למסף הבא
            }
        });
        Button btnSignUp = findViewById(R.id.btnCreateAccount);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_register);
                etUsername = findViewById(R.id.etUserName);
                etEmail = findViewById(R.id.etEmail);
                etPassword = findViewById(R.id.etPassword);
                etRepeatPassword =  findViewById(R.id.etRepeatPassword);
                Button btnRegister = findViewById(R.id.btnRegister);
                btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String username = etUsername.getText().toString().trim();
                        String email = etEmail.getText().toString().trim();
                        String password = etPassword.getText().toString();
                        String RepeatPassword = etRepeatPassword.getText().toString();
                        if (!password.equals(RepeatPassword)) {
                            Toast.makeText(MainActivity.this, R.string.passwords_match, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (username.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.please_put_in_an_username, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (email.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.please_put_in_an_email, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (password.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.please_fill_in_a_password, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !RepeatPassword.isEmpty() && password.equals(RepeatPassword)) {
                            saveLogin();
                            setContentView(R.layout.activity_main);
                            etEmail.setText(email);
                            etPassword.setText(password);
                        }
                    }
                });
            }
        });
    }
    private void saveLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
/*
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "יש למלא שם משתמש וסיסמה", Toast.LENGTH_SHORT).show();
            return;
        }
*/
        SharedPreferences.Editor editor = sp.edit();   // 3. פותחים עורך
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, pass);
        // save other things to database here!
//        else {
//            // לא רוצים לזכור? מוחקים את מה שאולי נשמר בעבר
//            editor.remove(KEY_USER);
//            editor.remove(KEY_PASS);
//            editor.putBoolean(KEY_REMEM, false);
//        }
        editor.apply(); // 4. בלי זה שום דבר לא נשמר!
    }
    private void forgetLogin() {
        sp.edit().clear().apply();      // מוחק את כל תוכן הקובץ
        // save this for signing out on activity_home
    }
}