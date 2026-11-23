package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

// Same idea as TaskStorage in finalstest, one level deeper:
// every list is a JSON object, and its "items" array holds games/albums tagged with "TypeName".
// The data is saved per Firebase uid, so two accounts on one phone don't see each other's lists.
public class ListStorage {
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final String uid;

    public ListStorage(Context context, String uid) {
        this.prefs = context.getSharedPreferences("lists_prefs", Context.MODE_PRIVATE);
        this.uid = uid;
    }

    public ArrayList<MediaList> loadAll() {
        ArrayList<MediaList> result = new ArrayList<>();
        String json = prefs.getString(uid, null);
        if (json == null) return result;

        JsonArray listsArray = gson.fromJson(json, JsonArray.class);
        for (JsonElement listElement : listsArray) {
            JsonObject listObj = listElement.getAsJsonObject();
            MediaList list = new MediaList(listObj.get("id").getAsInt(), listObj.get("name").getAsString());

            JsonArray itemsArray = listObj.getAsJsonArray("items");
            if (itemsArray != null) {
                for (JsonElement itemElement : itemsArray) {
                    JsonObject itemObj = itemElement.getAsJsonObject();
                    String type = itemObj.get("TypeName").getAsString();

                    MediaItem item;
                    if (type.equals("Game")) {
                        item = gson.fromJson(itemObj, Game.class);
                    } else {
                        item = gson.fromJson(itemObj, Album.class);
                    }
                    list.getItems().add(item);
                }
            }
            result.add(list);
        }
        return result;
    }

    public void saveAll(ArrayList<MediaList> lists) {
        JsonArray listsArray = new JsonArray();
        for (MediaList list : lists) {
            JsonObject listObj = new JsonObject();
            listObj.addProperty("id", list.getId());
            listObj.addProperty("name", list.getName());

            JsonArray itemsArray = new JsonArray();
            for (MediaItem item : list.getItems()) {
                JsonObject itemObj = gson.toJsonTree(item).getAsJsonObject();   // keeps the subclass fields
                itemObj.addProperty("TypeName", item.getTypeName());           // so we know what to rebuild
                itemsArray.add(itemObj);
            }
            listObj.add("items", itemsArray);
            listsArray.add(listObj);
        }
        prefs.edit().putString(uid, gson.toJson(listsArray)).apply();
    }

    public MediaList addList(String name) {
        ArrayList<MediaList> lists = loadAll();
        MediaList list = new MediaList(nextId(lists), name);
        lists.add(list);
        saveAll(lists);
        return list;
    }

    public MediaList findList(int id) {
        for (MediaList list : loadAll()) {
            if (list.getId() == id) return list;
        }
        return null;
    }

    // call this after you changed a list's items (add / remove / edit an item)
    public void saveList(MediaList changed) {
        ArrayList<MediaList> lists = loadAll();
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getId() == changed.getId()) {
                lists.set(i, changed);
                break;
            }
        }
        saveAll(lists);
    }

    public void deleteList(int id) {
        ArrayList<MediaList> lists = loadAll();
        lists.removeIf(l -> l.getId() == id);   // API 24+
        saveAll(lists);
    }

    private int nextId(ArrayList<MediaList> lists) {
        int max = -1;
        for (MediaList l : lists) max = Math.max(max, l.getId());
        return max + 1;
    }
}
