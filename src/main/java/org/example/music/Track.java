package org.example.music;

import java.util.Objects;

public class Track {
    private final String title;
    private final String artist;
    private final String path;

    public Track(String title, String artist, String path) {
        this.title = title != null ? title : "Unknown";
        this.artist = artist != null ? artist : "Unknown Artist";
        this.path = path;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(path, track.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}