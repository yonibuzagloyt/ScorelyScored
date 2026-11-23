package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// Screen 1 after login: a ListView of lists
public class HomeActivity extends AppCompatActivity {

    private ListStorage storage;
    private final ArrayList<MediaList> lists = new ArrayList<>();
    private ArrayAdapter<MediaList> adapter;
    private TextView tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {          // not logged in -> back to the login screen
            goToLogin();
            return;
        }
        storage = new ListStorage(this, user.getUid());

        TextView tvHello = findViewById(R.id.tvHello);
        tvTime = findViewById(R.id.tvTime);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        ListView lvTasks = findViewById(R.id.lvTasks);
        Button btnAddList = findViewById(R.id.btnAddList);
        Button btnLogout = findViewById(R.id.btnLogout);

        // the username was saved in the database when the user registered
        tvHello.setText("שלום!");
        FirebaseDatabase.getInstance(MainActivity.DATABASE_URL)
                .getReference("users").child(user.getUid()).child("username").get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    if (name != null) tvHello.setText("שלום " + name + "!");
                });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lists);
        lvTasks.setAdapter(adapter);
        lvTasks.setEmptyView(tvEmpty);

        // tap: open that list
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, MediaListActivity.class);
            intent.putExtra("LIST_ID", lists.get(position).getId());
            startActivity(intent);
        });

        // long press: delete that list
        lvTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(lists.get(position));
            return true;
        });

        btnAddList.setOnClickListener(v -> askForListName());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToLogin();
        });
    }

    // runs again when we come back from a list, so the counters stay up to date
    @Override
    protected void onResume() {
        super.onResume();
        if (storage != null) reload();
    }

    private void reload() {
        lists.clear();
        lists.addAll(storage.loadAll());
        adapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvTime.setText("השעה עכשיו: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    private void askForListName() {
        EditText input = new EditText(this);
        input.setHint("שם הרשימה");
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("רשימה חדשה")
                .setView(input)
                .setPositiveButton("הוסף", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "יש להזין שם", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    storage.addList(name);
                    reload();
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void confirmDelete(MediaList list) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת רשימה")
                .setMessage("למחוק את \"" + list.getName() + "\" ואת כל מה שבתוכה?")
                .setPositiveButton("מחק", (d, w) -> {
                    storage.deleteList(list.getId());
                    reload();
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void goToLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
