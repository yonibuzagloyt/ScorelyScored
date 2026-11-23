package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Screen 3: shows the item's title and lets the user write / save a review for it.
// The review lives in Firebase at: users/<uid>/reviews/<itemId>
public class ItemDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);   // <- use your actual file name for the third layout

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        int listId = getIntent().getIntExtra("LIST_ID", -1);
        int itemIndex = getIntent().getIntExtra("ITEM_INDEX", -1);
        if (user == null) {
            finish();
            return;
        }

        MediaList list = new ListStorage(this, user.getUid()).findList(listId);
        if (list == null || itemIndex < 0 || itemIndex >= list.getItems().size()) {
            finish();
            return;
        }
        MediaItem item = list.getItems().get(itemIndex);

        TextView tvJob = findViewById(R.id.tvJob);   // old id from another project, it's just the title here
        EditText etReview = findViewById(R.id.etReview);
        Button btnSave = findViewById(R.id.btnCall);  // the "save" button
        Button btnBack = findViewById(R.id.btnBack);

        tvJob.setText(item.getTitle());

        // the address of this item's review in the database
        DatabaseReference reviewRef = FirebaseDatabase.getInstance(MainActivity.DATABASE_URL)
                .getReference("users").child(user.getUid())
                .child("reviews").child(item.getId());

        // if a review was already saved, show it
        reviewRef.get().addOnSuccessListener(snapshot -> {
            String saved = snapshot.getValue(String.class);
            if (saved != null) etReview.setText(saved);
        });

        btnSave.setOnClickListener(v -> {
            String text = etReview.getText().toString().trim();

            if (text.isEmpty()) {
                // an empty review = delete the saved one
                reviewRef.removeValue()
                        .addOnSuccessListener(x -> Toast.makeText(this, "Review removed", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                reviewRef.setValue(text)
                        .addOnSuccessListener(x -> Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // TODO later: btnWeb (add to another list)
    }
}
