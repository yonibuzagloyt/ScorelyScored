package com.example.myapplication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Saves every list (and the games / albums inside it) in Firebase Realtime Database:
//
//   users/<uid>/lists/<listId>/id
//                             /name
//                             /items/0, 1, 2 ...   <- one game or album each, tagged with "TypeName"
//
// Because Firebase works over the network, every method returns a Task.
// Use .addOnSuccessListener(...) / .addOnFailureListener(...) / .addOnCompleteListener(...) on it.
// Data is stored per Firebase uid, so two accounts on one phone don't see each other's lists.
public class ListStorage {
    private final Gson gson = new Gson();
    private final DatabaseReference listsRef;

    public ListStorage(String uid) {
        this.listsRef = FirebaseDatabase.getInstance(MainActivity.DATABASE_URL)
                .getReference("users").child(uid).child("lists");
    }

    // ---------------------------------------------------------------- read

    public Task<ArrayList<MediaList>> loadAll() {
        return read(listsRef).continueWith(task -> {
            ArrayList<MediaList> result = new ArrayList<>();
            for (DataSnapshot listSnap : task.getResult().getChildren()) {
                MediaList list = parseList(listSnap);
                if (list != null) result.add(list);
            }
            return result;
        });
    }

    // the Task's result is null if there is no list with this id
    public Task<MediaList> findList(int id) {
        return read(listsRef.child(String.valueOf(id))).continueWith(task -> {
            DataSnapshot snap = task.getResult();
            return snap.exists() ? parseList(snap) : null;
        });
    }

    // ---------------------------------------------------------------- write

    public Task<MediaList> addList(String name) {
        return loadAll().continueWithTask(task -> {
            MediaList list = new MediaList(nextId(task.getResult()), name);
            return saveList(list).continueWith(t -> list);
        });
    }

    // call this after you changed a list's items (add / remove / edit an item)
    // only this one list is written, the others are untouched
    public Task<Void> saveList(MediaList list) {
        Map<String, Object> listMap = new LinkedHashMap<>();
        listMap.put("id", (long) list.getId());
        listMap.put("name", list.getName());

        List<Object> items = new ArrayList<>();
        for (MediaItem item : list.getItems()) {
            JsonObject itemObj = gson.toJsonTree(item).getAsJsonObject();   // keeps the subclass fields
            itemObj.addProperty("TypeName", item.getTypeName());           // so we know what to rebuild
            items.add(toFirebaseValue(itemObj));
        }
        listMap.put("items", items);

        return listsRef.child(String.valueOf(list.getId())).setValue(listMap);
    }

    public Task<Void> deleteList(int id) {
        return listsRef.child(String.valueOf(id)).removeValue();
    }

    // ---------------------------------------------------------------- helpers

    // Reads a location once. Unlike ref.get(), this waits for the connection to Firebase
    // instead of failing with "Client is offline" when the app has only just started.
    private Task<DataSnapshot> read(DatabaseReference ref) {
        TaskCompletionSource<DataSnapshot> source = new TaskCompletionSource<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                source.setResult(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                source.setException(error.toException());
            }
        });
        return source.getTask();
    }

    private MediaList parseList(DataSnapshot listSnap) {
        Long id = listSnap.child("id").getValue(Long.class);
        String name = listSnap.child("name").getValue(String.class);
        if (id == null || name == null) return null;

        MediaList list = new MediaList(id.intValue(), name);
        for (DataSnapshot itemSnap : listSnap.child("items").getChildren()) {
            Object raw = itemSnap.getValue();
            if (!(raw instanceof Map)) continue;

            JsonObject itemObj = gson.toJsonTree(raw).getAsJsonObject();
            JsonElement typeElement = itemObj.get("TypeName");
            String type = typeElement == null ? "" : typeElement.getAsString();

            MediaItem item;
            if (type.equals("Game")) {
                item = gson.fromJson(itemObj, Game.class);
            } else {
                item = gson.fromJson(itemObj, Album.class);
            }
            list.getItems().add(item);
        }
        return list;
    }

    // Gson tree -> plain Maps / Lists / Long / Double / Boolean / String, which is what Firebase accepts.
    // (Going through Gson's own Map conversion would turn every int into a double.)
    private Object toFirebaseValue(JsonElement element) {
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                if (!e.getValue().isJsonNull()) map.put(e.getKey(), toFirebaseValue(e.getValue()));
            }
            return map;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            List<Object> list = new ArrayList<>();
            for (JsonElement e : array) list.add(toFirebaseValue(e));
            return list;
        }
        JsonPrimitive p = element.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) {
            String s = p.getAsString();
            if (s.contains(".") || s.contains("e") || s.contains("E")) return p.getAsDouble();
            return p.getAsLong();
        }
        return p.getAsString();
    }

    private int nextId(ArrayList<MediaList> lists) {
        int max = -1;
        for (MediaList l : lists) max = Math.max(max, l.getId());
        return max + 1;
    }
}
