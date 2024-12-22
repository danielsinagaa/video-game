package com.hybrid.filter.video_game.service;

import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GenreService {
    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public List<GenreDTO> getAllGenresDTO() {
        List<GenreDTO> genresDTO = new ArrayList<>();
        genreRepository.findAll().forEach(genre -> {
            GenreDTO genreDTO = new GenreDTO();
            genreDTO.setId(genre.getId());
            genreDTO.setName(genre.getName());
            genreDTO.setGenreImage(genre.getGenreImage());
            genresDTO.add(genreDTO);
        });
        return genresDTO;
    }

    public Genre getGenreById(int id){
        return genreRepository.findById(id).get();
    }

    public Genre saveGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre saveGenre(String name, MultipartFile genreImage) throws IOException {
        Genre genre = new Genre();
        try {
            genre.setName(name);

            // Cek ukuran file sebelum menyimpannya
            long maxSize = 10 * 1024 * 1024; // 10 MB
            if (genreImage.getSize() > maxSize) {
                throw new IOException("File size exceeds maximum allowed size of 10 MB");
            }

            // Mengonversi gambar menjadi byte[] dan menyimpannya dalam kolom genre_image
            byte[] genreImageBytes = genreImage.getBytes();
            genre.setGenreImage(genreImageBytes); // Set gambar dalam bentuk byte[]

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to save genre image", e);
        }
        return genreRepository.save(genre);
    }

}
