package com.example.myapplication;

import java.util.ArrayList;

public class Album extends MediaItem
{
    private String Artist;
    private String Genre;
    private boolean Explicit;
    private boolean isEP;
    private ArrayList<String> Tracks;
    private int Runtime;
    public Album(String title, String artist, String genre, boolean explicit, boolean isEP, ArrayList<String> tracks, int runtime, int year, int month, int day, int score)
    {
        super(title, year, month, day, score);
        this.Artist = artist;
        this.Genre = genre;
        this.Explicit = explicit;
        this.isEP = isEP;
        this.Tracks = tracks;
        this.Runtime = runtime;
    }

    public int getRuntime() {
        return Runtime;
    }

    public void setRuntime(int runtime) {
        Runtime = runtime;
    }

    public ArrayList<String> getTracks() {
        return Tracks;
    }

    public void setTracks(ArrayList<String> tracks) {
        Tracks = tracks;
    }

    public boolean isEP() {
        return isEP;
    }

    public void setEP(boolean EP) {
        isEP = EP;
    }

    public boolean isExplicit() {
        return Explicit;
    }

    public void setExplicit(boolean explicit) {
        Explicit = explicit;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }
    public void AddTrack(String Track)
    {
        this.Tracks.add(Track);
    }

}
