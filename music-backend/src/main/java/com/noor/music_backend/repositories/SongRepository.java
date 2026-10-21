package com.noor.music_backend.repositories;

import com.noor.music_backend.models.Song;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SongRepository extends JpaRepository<Song, Long> {

}
