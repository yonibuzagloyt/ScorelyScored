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
    private static final String KEY_EMAIL  = "email";
    private static final String KEY_PASSWORD  = "password";
    private EditText etEmail, etPassword;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail =

        // 1. משיגים את קובץ ההעדפות

        sp = getSharedPreferences(PREFS, MODE_PRIVATE);


        // 2. טוענים מה שנשמר בפעם הקודמת (אם נשמר)

        boolean remember = sp.getBoolean(KEY_REMEM, false);

        cbRemember.setChecked(remember);
        if (remember) {

            etUser.setText(sp.getString(KEY_USER, ""));


            etPass.setText(sp.getString(KEY_PASS, ""));

            tvStatus.setText("הפרטים נטענו מהזיכרון ✔");
        }

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLogin();
            }
        });

        Button btnForget = findViewById(R.id.btnForget);
        btnForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetLogin();
            }
        });
    }

    private void saveLogin() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "יש למלא שם משתמש וסיסמה", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences.Editor editor = sp.edit();   // 3. פותחים עורך


        if (cbRemember.isChecked()) {


            editor.putString(KEY_USER, user);


            editor.putString(KEY_PASS, pass);


            editor.putBoolean(KEY_REMEM, true);

        } else {
            // לא רוצים לזכור? מוחקים את מה שאולי נשמר בעבר
            editor.remove(KEY_USER);
            editor.remove(KEY_PASS);
            editor.putBoolean(KEY_REMEM, false);
        }

        editor.apply();                                // 4. בלי זה שום דבר לא נשמר!


        tvStatus.setText("שלום " + user + " · הפרטים " +
                (cbRemember.isChecked() ? "נשמרו" : "לא נשמרו"));
    }

    private void forgetLogin() {

        sp.edit().clear().apply();      // מוחק את כל תוכן הקובץ

        etUser.setText("");
        etPass.setText("");
        cbRemember.setChecked(false);
        tvStatus.setText("כל הפרטים נמחקו");
    }
}