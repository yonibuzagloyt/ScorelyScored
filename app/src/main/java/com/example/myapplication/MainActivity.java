package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // שמות המפתחות כקבועים — כדי שלא נשגה בהקלדה במקום אחד מתוך שניים
    private static final String PREFS     = "login_prefs";
    private static final String KEY_EMAIL  = "email";
    private static final String KEY_PASSWORD  = "password";
    private EditText etEmail, etPassword, etRepeatPassword, etUsername;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // 1. משיגים את קובץ ההעדפות

        sp = getSharedPreferences(PREFS, MODE_PRIVATE);

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
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                etRepeatPassword =  findViewById(R.id.etRepeatPassword);
                String RepeatPassword = etRepeatPassword.getText().toString();
                if (!password.equals(RepeatPassword))
                {
                    Toast.makeText(MainActivity.this, R.string.passwords_match, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (username.isEmpty())
                {
                    Toast.makeText(MainActivity.this, R.string.please_put_in_an_username, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (email.isEmpty())
                {
                    Toast.makeText(MainActivity.this, R.string.please_put_in_an_email, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty())
                {
                    Toast.makeText(MainActivity.this, R.string.please_fill_in_a_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !RepeatPassword.isEmpty())
                {
                    saveLogin();
                }
            }
        });
    }

    private void saveLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

//        if (user.isEmpty() || pass.isEmpty()) {
//            Toast.makeText(this, "יש למלא שם משתמש וסיסמה", Toast.LENGTH_SHORT).show();
//            return;
//        }


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

        editor.apply();                                // 4. בלי זה שום דבר לא נשמר!
    }

    private void forgetLogin() {

        sp.edit().clear().apply();      // מוחק את כל תוכן הקובץ
        // save this for signing out on activity_home
    }
}