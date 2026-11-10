package com.example.myapplication;

import androidx.annotation.NonNull;

import java.security.PrivateKey;
import java.util.ArrayList;

public class Game extends MediaItem
{
    private String Developer;
    private String Publisher;
    private ArrayList<String> Platforms;
    private Game(String title, String developer, String publisher, int year, int month, int day, int score, ArrayList<String> platforms)
    {
        super(title, year, month, day, score);
        this.Developer = developer;
        this.Publisher = publisher;
        this.Platforms = platforms;
    }

    public String getDeveloper() {
        return this.Developer;
    }

    public void setDeveloper(String developer) {
        this.Developer = developer;
    }
    public String getPublisher() {
        return this.Publisher;
    }
    public void setPublisher(String publisher) {
        this.Publisher = publisher;
    }
    public ArrayList<String> getPlatforms() {
        return this.Platforms;
    }
    public void setPlatforms(ArrayList<String> platforms) {
        this.Platforms = platforms;
    }
    public void AddPlatform(String Platform)
    {
        this.Platforms.add(Platform);
    }
    @NonNull
    @Override
    public String toString() {
        return this.getTitle() + " - " + this.getDeveloper() + " - " + this.getPublisher() + " (" + this.getYear() + ")" + "\n Platforms: " + this.getPlatforms() + "\n Score: " + this.getScore() + "\n Tags: " + getTags();
    }
}
