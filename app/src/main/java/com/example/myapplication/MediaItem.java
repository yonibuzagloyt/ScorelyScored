package com.example.myapplication;

import java.util.ArrayList;
import java.util.UUID;

public abstract class MediaItem
{
    private int Year;
    private int Month;
    private int Day;
    private String Title;
    private int Score;
    private ArrayList<String> Tags = new ArrayList<>();   // was null before -> AddTag crashed
    private String Id = UUID.randomUUID().toString();     // never changes, so a review can't end up on the wrong item

    public MediaItem(String title, int year, int month, int day, int score)
    {
        this.Title = title;
        this.Year = year;
        this.Month = month;
        this.Day = day;
        this.Score = score;
    }

    // "Game" / "Album": saved next to the item so ListStorage knows which class to rebuild
    public abstract String getTypeName();

    public String getId() {
        if (this.Id == null) this.Id = UUID.randomUUID().toString();   // items saved before ids existed
        return this.Id;
    }
    public boolean hasId() { return this.Id != null; }

    public int getDay() { return this.Day; }
    public void setDay(int day) { this.Day = day; }

    public int getYear() { return this.Year; }
    public void setYear(int year) { this.Year = year; }

    public int getMonth() { return this.Month; }
    public void setMonth(int month) { this.Month = month; }

    public String getTitle() { return this.Title; }
    public void setTitle(String title) { this.Title = title; }

    public int getScore() { return this.Score; }
    public void setScore(int score) { this.Score = score; }

    public ArrayList<String> getTags() {
        if (this.Tags == null) this.Tags = new ArrayList<>();
        return this.Tags;
    }
    public void setTags(ArrayList<String> tags) { this.Tags = tags; }
    public void AddTag(String Tag) { getTags().add(Tag); }
}
