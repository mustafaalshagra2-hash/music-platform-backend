package com.noor.music_backend.services;

import com.noor.music_backend.models.Song;
import com.noor.music_backend.repositories.SongRepository;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class SongService {

    private final Path fileStorageLocation;
    
    @Autowired
    private SongRepository songRepository;

    public SongService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory.", ex);
        }
    }

    public Song saveSong(MultipartFile file) {
        
        String originalFilename = file.getOriginalFilename();
        
        if (originalFilename == null) {
            throw new RuntimeException("Error: File name is null");
        }
        
        String fileName = StringUtils.cleanPath(originalFilename);

        try {
            if(fileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Song song = new Song();
            song.setTitle(fileName);
            song.setFilePath(targetLocation.toString());

            // Winamp Logic: Extract Metadata
            try {
                File audioFile = targetLocation.toFile();
                AudioFile f = AudioFileIO.read(audioFile);
                Tag tag = f.getTag();

                if (tag != null) {
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    if (artist != null && !artist.isEmpty()) song.setArtist(artist);

                    String album = tag.getFirst(FieldKey.ALBUM);
                    if (album != null && !album.isEmpty()) song.setAlbum(album);

                    String genre = tag.getFirst(FieldKey.GENRE);
                    if (genre != null && !genre.isEmpty()) song.setGenre(genre);

                    try {
                        String yearStr = tag.getFirst(FieldKey.YEAR);
                        if (yearStr != null && !yearStr.isEmpty()) {
                            song.setReleaseYear(Integer.parseInt(yearStr));
                        }
                    } catch (Exception e) {
                        // Ignore parsing error
                    }

                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null) {
                        String artFileName = fileName + "_cover.jpg";
                        Path artLocation = this.fileStorageLocation.resolve(artFileName);
                        Files.write(artLocation, artwork.getBinaryData());
                        song.setCoverArtPath(artLocation.toString());
                    }
                }
                
                int durationSeconds = f.getAudioHeader().getTrackLength();
                long minutes = durationSeconds / 60;
                long seconds = durationSeconds % 60;
                song.setDuration(String.format("%02d:%02d", minutes, seconds));

            } catch (Exception e) {
                System.out.println("Metadata extraction failed, saving raw file.");
            }

            return songRepository.save(song);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found with id " + id));
    }
}