package com.hybrid.filter.video_game.repository;

import com.hybrid.filter.video_game.model.entity.GameGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameGenreRepository extends JpaRepository<GameGenre, GameGenre.GameGenreId> {
    // Custom query methods can be added here if needed
    List<GameGenre> findByGameId(Integer game_id);
    List<GameGenre> findByGenreId(Integer game_id);
}