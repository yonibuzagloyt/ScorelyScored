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

        // the back button works right away, even while the list is still loading
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // the list now comes from Firebase, so wait for it before showing the item
        new ListStorage(user.getUid()).findList(listId)
                .addOnSuccessListener(this, list -> {
                    if (list == null || itemIndex < 0 || itemIndex >= list.getItems().size()) {
                        finish();
                        return;
                    }
                    showItem(user, list.getItems().get(itemIndex));
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void showItem(FirebaseUser user, MediaItem item) {
        TextView tvJob = findViewById(R.id.tvJob);   // old id from another project, it's just the title here
        EditText etReview = findViewById(R.id.etReview);
        Button btnSave = findViewById(R.id.btnCall);  // the "save" button

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
                        .addOnSuccessListener(x -> Toast.makeText(this, R.string.review_removed, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                reviewRef.setValue(text)
                        .addOnSuccessListener(x -> Toast.makeText(this, R.string.review_saved, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        // TODO later: btnWeb (add to another list)
    }
}
