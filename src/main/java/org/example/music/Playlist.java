package org.example.music;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist {
    private final String name;
    private final ObservableList<Track> tracks = FXCollections.observableArrayList();

    public Playlist(String name) {
        this.name = name != null ? name.trim() : "Новый плейлист";
    }

    public String getName() {
        return name;
    }

    public ObservableList<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        if (track != null && !tracks.contains(track)) {
            tracks.add(track);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}