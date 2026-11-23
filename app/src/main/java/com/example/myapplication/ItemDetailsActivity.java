package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Screen 3: left almost blank on purpose. It only finds the item and shows its title.
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
        tvJob.setText(item.getTitle());

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // TODO later: etReview + btnCall (save) + btnWeb (add to another list)
    }
}
