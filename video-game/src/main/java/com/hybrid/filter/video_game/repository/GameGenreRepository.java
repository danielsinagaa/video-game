package com.hybrid.filter.video_game.repository;

import com.hybrid.filter.video_game.model.entity.GameGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameGenreRepository extends JpaRepository<GameGenre, Integer> {
    // Metode untuk mendapatkan data berdasarkan game_id atau genre_id
    List<GameGenre> findByGameId(Integer gameId);
    List<GameGenre> findByGenreId(Integer genreId);
}