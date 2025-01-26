package com.hybrid.filter.video_game.repository;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    @Query("SELECT g FROM Game g JOIN g.genres gen WHERE gen IN :genres")
    List<Game> findByGenres(@Param("genres") List<Genre> genres);

    List<Game> findByTitleContaining(String title);

    @Query("SELECT g FROM Game g WHERE g.title LIKE %:title%")
    List<Game> searchByTitleLike(@Param("title") String title);

    List<Game> findByPrice(Double price);


    List<Game> findByReleaseDateBetween(Date startDate, Date endDate);
}
