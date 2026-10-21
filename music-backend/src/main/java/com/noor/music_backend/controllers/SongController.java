package com.noor.music_backend.controllers;

import com.noor.music_backend.models.Song;
import com.noor.music_backend.services.SongService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongService songService;

    // 1. 📤 Upload Song (No need for title/artist params, we extract them automatically)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSong(@RequestParam("file") MultipartFile file) {
        try {
            Song savedSong = songService.saveSong(file);
            return ResponseEntity.ok(savedSong);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    // 2. 📜 Get all songs
    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    // 3. 🔍 Get song details by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getSongById(@PathVariable Long id) {
        try {
            Song song = songService.getSongById(id);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found");
        }
    }

    // 4. 🎵 Play Song (Streaming - Winamp Style 🦁)
    @GetMapping("/play/{id}")
    public ResponseEntity<Resource> playSong(@PathVariable Long id, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Song song = songService.getSongById(id);
            File file = new File(song.getFilePath());

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            long fileLength = file.length();

            // Case 1: Normal request (No seeking)
            if (rangeHeader == null) {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                        .body(resource);
            }

            // Case 2: Range request (Seeking/Streaming)
            HttpRange range = HttpRange.parseRanges(rangeHeader).get(0);
            long start = range.getRangeStart(fileLength);
            long end = range.getRangeEnd(fileLength);
            long contentLength = end - start + 1;

            FileInputStream inputStream = new FileInputStream(file);
            inputStream.skip(start); // Skip to the requested byte

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) // 206 Partial Content
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 5. 🖼️ Get Cover Art
    @GetMapping("/cover/{id}")
    public ResponseEntity<Resource> getSongCover(@PathVariable Long id) {
        try {
            Song song = songService.getSongById(id);

            if (song.getCoverArtPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Path coverPath = Paths.get(song.getCoverArtPath());
            Resource resource = new UrlResource(coverPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}