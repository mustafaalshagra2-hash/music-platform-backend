package com.noor.music_backend.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // اسم القائمة (مثلاً: راب)

    // ربط القائمة بصاحبها (المستخدم)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ربط القائمة بالأغاني (تحتوي على أغاني متعددة)
    @ManyToMany
    @JoinTable(
        name = "playlist_songs",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

    // --- Constructors ---
    public Playlist() {}

    public Playlist(String name, User user) {
        this.name = name;
        this.user = user;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }
    
    // دالة مساعدة لإضافة أغنية للقائمة
    public void addSong(Song song) {
        this.songs.add(song);
    }

    public void removeSong(Song song) {
        this.songs.remove(song);
    }
}