package com.noor.music_backend.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // اسم الفولدر اللي نخزن بي الملفات
    private static final String UPLOAD_DIR = "uploads";

    // 1. دالة تجيب قائمة بكل الملفات الموجودة (JSON)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles() {
        File folder = new File(UPLOAD_DIR);
        List<Map<String, Object>> fileList = new ArrayList<>();

        // نتأكد أن الفولدر موجود
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    // حجم الملف بالكيلوبايت
                    fileInfo.put("size", (file.length() / 1024) + " KB");
                    fileInfo.put("isDir", file.isDirectory());
                    
                    // رابط مباشر للملف (مفيد للفرونت إند)
                    // نستخدم البوابة 8080 او المباشر 8081 حسب طريقة اتصالك
                    // هنا نرجع بس الاسم والفرونت يركب الرابط
                    
                    fileList.add(fileInfo);
                }
            }
        }
        return ResponseEntity.ok(fileList);
    }

    // 2. دالة لعرض الملف (صورة) أو تشغيله (صوت) بالمتصفح
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // نحدد مكان الملف
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                // نحاول نعرف نوع الملف (image/png, audio/mpeg, etc.)
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream"; // نوع عام اذا ما عرفنا
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // "inline" يعني اعرضه بالمتصفح، "attachment" يعني حمله
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    // 3. دالة لحذف الملف
    @DeleteMapping("/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            File file = filePath.toFile();

            if (file.exists()) {
                if (file.delete()) {
                    return ResponseEntity.ok("Deleted: " + filename);
                } else {
                    return ResponseEntity.status(500).body("Could not delete the file.");
                }
            } else {
                return ResponseEntity.status(404).body("File not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}