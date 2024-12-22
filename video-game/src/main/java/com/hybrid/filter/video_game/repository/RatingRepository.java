package com.hybrid.filter.video_game.repository;

import com.hybrid.filter.video_game.model.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    List<Rating> findByUserId(Integer userId);
    List<Rating> findByGameId(Integer gameId);
    Rating findByUserIdAndGameId(Integer userId, Integer gameId);
}

