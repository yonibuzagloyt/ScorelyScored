package com.example.myapplication;

import androidx.annotation.NonNull;

import java.util.ArrayList;

// One entry on the home screen. It holds its own games and albums.
public class MediaList
{
    private int id;
    private String name;
    private ArrayList<MediaItem> items = new ArrayList<>();

    public MediaList(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ArrayList<MediaItem> getItems() { return items; }

    public int countGames() {
        int n = 0;
        for (MediaItem item : items) if (item instanceof Game) n++;
        return n;
    }

    public int countAlbums() {
        int n = 0;
        for (MediaItem item : items) if (item instanceof Album) n++;
        return n;
    }

    // the home screen's ListView shows this
    @NonNull
    @Override
    public String toString() {
        return name + "\n items: " + items.size() + " | games: " + countGames() + " | albums: " + countAlbums();
    }
}
