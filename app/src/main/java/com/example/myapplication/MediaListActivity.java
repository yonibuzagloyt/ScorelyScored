package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

// Screen 2: the games and albums inside one list
public class MediaListActivity extends AppCompatActivity {

    private ListStorage storage;
    private int listId;

    private TextView tvListTitle, tvStats;
    private final ArrayList<MediaItem> shownItems = new ArrayList<>();
    private ArrayAdapter<MediaItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        listId = getIntent().getIntExtra("LIST_ID", -1);
        if (user == null || listId == -1) {
            finish();
            return;
        }
        storage = new ListStorage(this, user.getUid());

        tvListTitle = findViewById(R.id.tvListTitle);
        tvStats = findViewById(R.id.tvStats);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        ListView lvTasks = findViewById(R.id.lvTasks);
        Button btnFillExample = findViewById(R.id.btnFillExample);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shownItems);
        lvTasks.setAdapter(adapter);
        lvTasks.setEmptyView(tvEmpty);

        // tap: open the (for now blank) details screen
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MediaListActivity.this, ItemDetailsActivity.class);
            intent.putExtra("LIST_ID", listId);
            intent.putExtra("ITEM_INDEX", position);
            startActivity(intent);
        });

        // long press: delete the item
        lvTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(position);
            return true;
        });

        btnFillExample.setOnClickListener(v -> fillExample());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storage != null) reload();
    }

    private void reload() {
        MediaList list = storage.findList(listId);
        if (list == null) {      // the list was deleted
            finish();
            return;
        }
        tvListTitle.setText(list.getName());   // the title comes from the code, not the XML
        tvStats.setText("items in total : " + list.getItems().size()
                + " | games : " + list.countGames()
                + " | albums : " + list.countAlbums());

        shownItems.clear();
        shownItems.addAll(list.getItems());
        adapter.notifyDataSetChanged();
    }

    private void fillExample() {
        MediaList list = storage.findList(listId);
        if (list == null) return;

        list.getItems().add(new Game("Hades", "Supergiant Games", "Supergiant Games",
                2020, 9, 17, 95, new ArrayList<>(Arrays.asList("PC", "Switch", "PS5"))));

        list.getItems().add(new Album("In Rainbows", "Radiohead", "Alternative rock",
                false, false, new ArrayList<>(Arrays.asList("15 Step", "Bodysnatchers", "Nude")),
                42, 2007, 10, 10, 96));

        storage.saveList(list);   // changed the items -> save the whole list back
        reload();
    }

    private void confirmDelete(int position) {
        MediaItem item = shownItems.get(position);
        new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("למחוק את \"" + item.getTitle() + "\"?")
                .setPositiveButton("מחק", (d, w) -> {
                    MediaList list = storage.findList(listId);
                    if (list != null && position < list.getItems().size()) {
                        list.getItems().remove(position);
                        storage.saveList(list);
                    }
                    reload();
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}
