// Note.java
package com.example.diarynotes;

public class Note {
    private String date;
    private String note;
    private String imageUrl;

    public Note(String date, String note, String imageUrl) {
        this.date = date;
        this.note = note;
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

