package com.noor.music_backend.controllers;

import com.noor.music_backend.models.*;
import com.noor.music_backend.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public PlaylistController(PlaylistRepository playlistRepository, UserRepository userRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    // 1. إنشاء قائمة جديدة (مثلاً: راب)
    @PostMapping("/create")
    public ResponseEntity<Playlist> createPlaylist(@RequestParam String name, Authentication authentication) {
        // نجيب المستخدم الحالي المسجل دخول
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        Playlist playlist = new Playlist(name, user);
        return ResponseEntity.ok(playlistRepository.save(playlist));
    }

    // 2. إضافة أغنية إلى قائمة
    @PostMapping("/{playlistId}/add/{songId}")
    public ResponseEntity<String> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new RuntimeException("the playlist is not found"));
        Song song = songRepository.findById(songId).orElseThrow(() -> new RuntimeException("the song is not found"));

        playlist.addSong(song);
        playlistRepository.save(playlist);

        return ResponseEntity.ok("the song has been added to the playlist.");
    }

    // 3. عرض قوائم المستخدم الحالي فقط
    @GetMapping("/my-playlists")
    public List<Playlist> getMyPlaylists(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return playlistRepository.findByUser(user);
    }
    
    // 4. عرض أغاني داخل قائمة معينة
    @GetMapping("/{playlistId}")
    public ResponseEntity<Playlist> getPlaylistDetails(@PathVariable Long playlistId) {
         return ResponseEntity.of(playlistRepository.findById(playlistId));
    }
}