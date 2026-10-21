package com.noor.music_backend.repositories;

import com.noor.music_backend.models.Playlist;
import com.noor.music_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
// Find playlists by user
    List<Playlist> findByUser(User user);
}