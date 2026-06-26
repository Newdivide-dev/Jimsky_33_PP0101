package org.example.music;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;

public class MainController {

    @FXML private TableView<Track> tracksTable;
    @FXML private TableColumn<Track, String> titleColumn;
    @FXML private TableColumn<Track, String> artistColumn;
    @FXML private Slider progressSlider;
    @FXML private Label timeCurrent, timeTotal, nowPlayingLabel;
    @FXML private TextField searchField;
    @FXML private Slider volumeSlider;
    @FXML private Label currentUserLabel;
    @FXML private Button playButton;
    @FXML private ListView<Playlist> playlistsListView;
    @FXML private Button repeatButton;           // <-- Новая кнопка

    private final ObservableList<Track> libraryTracks = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private Playlist currentPlaylist = null;
    private MediaPlayer mediaPlayer;
    private Track currentPlayingTrack = null;

    private enum RepeatMode { OFF, REPEAT_ONE, REPEAT_ALL }
    private RepeatMode repeatMode = RepeatMode.OFF;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        tracksTable.setItems(libraryTracks);

        searchField.textProperty().addListener((obs, old, newVal) -> filterTracks(newVal));

        if (volumeSlider != null) {
            volumeSlider.setValue(80);
            volumeSlider.valueProperty().addListener((obs, old, newVal) -> {
                if (mediaPlayer != null) mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            });
        }

        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
        });

        if (currentUserLabel != null && UserSession.getUsername() != null) {
            currentUserLabel.setText("👤 " + UserSession.getUsername());
        }

        if (playlistsListView != null) {
            playlistsListView.setItems(playlists);
            playlistsListView.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "🎵 " + item.getName());
                }
            });
            playlistsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
                if (newP != null) {
                    currentPlaylist = newP;
                    searchField.clear();
                    tracksTable.setItems(newP.getTracks());
                }
            });
        }

        setupContextMenu();

        tracksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Track selected = tracksTable.getSelectionModel().getSelectedItem();
                if (selected != null) playTrack(selected);
            }
        });

        nowPlayingLabel.setText("Выберите трек для воспроизведения");
        playButton.setText("▶");
        if (repeatButton != null) repeatButton.setText("➡️");
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem playItem = new MenuItem("▶ Воспроизвести");
        playItem.setOnAction(e -> {
            Track sel = tracksTable.getSelectionModel().getSelectedItem();
            if (sel != null) playTrack(sel);
        });
        MenuItem addToPlItem = new MenuItem("➕ Добавить в плейлист...");
        addToPlItem.setOnAction(e -> showAddToPlaylistDialog());
        MenuItem deleteItem = new MenuItem("🗑 Удалить");
        deleteItem.setOnAction(e -> handleDeleteTrack());
        contextMenu.getItems().addAll(playItem, addToPlItem, new SeparatorMenuItem(), deleteItem);
        tracksTable.setContextMenu(contextMenu);
    }

    private void filterTracks(String query) {
        ObservableList<Track> source = getCurrentSourceList();
        if (query == null || query.trim().isEmpty()) {
            tracksTable.setItems(source);
        } else {
            String q = query.toLowerCase().trim();
            tracksTable.setItems(source.filtered(t ->
                    t.getTitle().toLowerCase().contains(q) || t.getArtist().toLowerCase().contains(q)));
        }
    }

    private ObservableList<Track> getCurrentSourceList() {
        return currentPlaylist != null ? currentPlaylist.getTracks() : libraryTracks;
    }

    @FXML private void handleShowLibrary() {
        currentPlaylist = null;
        playlistsListView.getSelectionModel().clearSelection();
        searchField.clear();
        tracksTable.setItems(libraryTracks);
    }

    @FXML
    private void handleCreatePlaylist() {
        TextInputDialog dialog = new TextInputDialog("Новый плейлист");
        dialog.setTitle("Создание плейлиста");
        dialog.setHeaderText("Введите название плейлиста");
        dialog.setContentText("Название:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Playlist newPl = new Playlist(name.trim());
                playlists.add(newPl);
                currentPlaylist = newPl;
                playlistsListView.getSelectionModel().select(newPl);
                tracksTable.setItems(newPl.getTracks());
                searchField.clear();
            }
        });
    }

    private void showAddToPlaylistDialog() {
        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (playlists.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Сначала создайте плейлист!").showAndWait();
            return;
        }
        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlists.get(0), playlists);
        dialog.setTitle("Добавить в плейлист");
        dialog.setHeaderText("Выберите плейлист");
        dialog.setContentText("Добавить трек \"" + selected.getTitle() + "\":");
        dialog.showAndWait().ifPresent(pl -> pl.addTrack(selected));
    }

    @FXML
    private void handleImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите аудиофайлы");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Аудио", "*.mp3", "*.wav", "*.m4a", "*.flac", "*.ogg"));
        java.util.List<File> files = fc.showOpenMultipleDialog(null);
        if (files != null) {
            for (File f : files) {
                String title = f.getName().contains(".") ? f.getName().substring(0, f.getName().lastIndexOf('.')) : f.getName();
                Track t = new Track(title, "Unknown Artist", f.toURI().toString());
                if (!libraryTracks.contains(t)) libraryTracks.add(t);
            }
        }
    }

    @FXML
    private void handleDeleteTrack() {
        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String where = currentPlaylist != null ? "из плейлиста" : "из библиотеки";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить \"" + selected.getTitle() + "\" " + where + "?");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (currentPlaylist != null) {
                    currentPlaylist.getTracks().remove(selected);
                } else {
                    libraryTracks.remove(selected);
                    playlists.forEach(pl -> pl.getTracks().remove(selected));
                }
                if (currentPlayingTrack != null && currentPlayingTrack.equals(selected)) {
                    stopPlayback();
                }
            }
        });
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        currentPlayingTrack = null;
        nowPlayingLabel.setText("Воспроизведение остановлено");
        playButton.setText("▶");
        progressSlider.setValue(0);
        timeCurrent.setText("0:00");
        timeTotal.setText("0:00");
    }

    @FXML
    private void handlePlayPause() {
        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (currentPlayingTrack != null && currentPlayingTrack.equals(selected) && mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("▶");
            } else {
                mediaPlayer.play();
                playButton.setText("⏸");
            }
        } else {
            playTrack(selected);
        }
    }

    private void playTrack(Track track) {
        if (track == null) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            mediaPlayer = new MediaPlayer(new Media(track.getPath()));
            currentPlayingTrack = track;
            tracksTable.getSelectionModel().select(track);

            mediaPlayer.setOnEndOfMedia(() -> {
                switch (repeatMode) {
                    case REPEAT_ONE:
                        playTrack(currentPlayingTrack);
                        break;
                    case REPEAT_ALL:
                        playNext();
                        break;
                    case OFF:
                    default:
                        playNext();
                        break;
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newT.toSeconds());
                    timeCurrent.setText(formatTime(newT));
                }
            });

            mediaPlayer.setOnReady(() -> {
                progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                timeTotal.setText(formatTime(mediaPlayer.getTotalDuration()));
                nowPlayingLabel.setText("▶ " + track.getTitle() + " — " + track.getArtist());
                playButton.setText("⏸");
            });

            mediaPlayer.setOnError(() -> {
                nowPlayingLabel.setText("Ошибка: " + track.getTitle());
                playButton.setText("▶");
                new Alert(Alert.AlertType.ERROR, "Не удалось воспроизвести файл").showAndWait();
            });

            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            mediaPlayer.play();
            nowPlayingLabel.setText("Загрузка... " + track.getTitle());

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка MediaPlayer").showAndWait();
        }
    }

    private void playNext() {
        ObservableList<Track> items = tracksTable.getItems();
        if (items == null || items.isEmpty()) return;

        int currentIdx = currentPlayingTrack != null ? items.indexOf(currentPlayingTrack) : -1;
        int nextIdx = (currentIdx + 1) % items.size();
        playTrack(items.get(nextIdx));
    }

    @FXML private void handleNext() { playNext(); }

    @FXML
    private void handlePrev() {
        ObservableList<Track> items = tracksTable.getItems();
        if (items == null || items.isEmpty()) return;
        int currentIdx = currentPlayingTrack != null ? items.indexOf(currentPlayingTrack) : -1;
        int prevIdx = (currentIdx - 1 + items.size()) % items.size();
        playTrack(items.get(prevIdx));
    }

    @FXML
    private void handleRepeat() {
        if (repeatButton == null) return;

        switch (repeatMode) {
            case OFF:
                repeatMode = RepeatMode.REPEAT_ONE;
                repeatButton.setText("🔂");
                break;
            case REPEAT_ONE:
                repeatMode = RepeatMode.REPEAT_ALL;
                repeatButton.setText("🔁");
                break;
            case REPEAT_ALL:
                repeatMode = RepeatMode.OFF;
                repeatButton.setText("➡️");
                break;
        }
    }

    @FXML
    private void handleLogout() {
        stopPlayback();
        UserSession.logout();
        MainApp.showScene("auth-view.fxml", "SoundWave - Вход");
    }

    private String formatTime(Duration d) {
        if (d == null) return "0:00";
        return String.format("%d:%02d", (int) d.toMinutes(), (int) d.toSeconds() % 60);
    }

    public ObservableList<Track> getLibraryTracksForTest() { return libraryTracks; }
}