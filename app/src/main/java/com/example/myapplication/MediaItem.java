package com.example.myapplication;

import java.util.ArrayList;

public abstract class MediaItem
{
    private int Year;
    private int Month;
    private int Day;
    private String Title;
    private ArrayList<String> Tags;
    private MediaItem(String title, int year, int month, int day)
    {
        this.Title = title;
        this.Year = year;
        this.Month = month;
        this.Day = day;
    }

    public int getDay() {
        return this.Day;
    }

    public void setDay(int day) {
        this.Day = day;
    }

    public int getYear() {
        return this.Year;
    }

    public void setYear(int year) {
        this.Year = year;
    }

    public int getMonth() {
        return this.Month;
    }

    public void setMonth(int month) {
        this.Month = month;
    }

    public String getTitle() {
        return this.Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public ArrayList<String> getTags() {
        return this.Tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.Tags = tags;
    }
    public void AddTag(String Tag)
    {
        this.Tags.add(Tag);
    }
}
