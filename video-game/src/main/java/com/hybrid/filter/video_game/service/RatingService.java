package com.hybrid.filter.video_game.service;
import com.hybrid.filter.video_game.model.dto.RatingDTO;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    public Rating save(Rating rating) {
        return ratingRepository.save(rating);
    }

    public List<Rating> findByUserId(int userId) {
        return ratingRepository.findByUserId(userId);
    }

    public Rating findByUserIdAndGameId(int userId, int gameId) {
        return ratingRepository.findByUserIdAndGameId(userId, gameId);
    }

    public List<Rating> getRating(int gameId) {
        return ratingRepository.findByGameId(gameId);
    }

    public String addRating(RatingDTO ratingDTO) {
        // Mencari rating yang ada untuk user dan game
        Rating existingRating = ratingRepository.findByUserIdAndGameId(ratingDTO.getUserId(), ratingDTO.getGameId());

        if (existingRating != null) {
            // Jika sudah ada, update rating
            existingRating.setRatingValue(ratingDTO.getRatingValue());
            ratingRepository.save(existingRating);
            return "Rating updated successfully";
        } else {
            // Jika belum ada, buat rating baru
            Rating newRating = new Rating();
            newRating.setUser(userRepository.findById(ratingDTO.getUserId()).orElseThrow());
            newRating.setGame(gameRepository.findById(ratingDTO.getGameId()).orElseThrow());
            newRating.setRatingValue(ratingDTO.getRatingValue());
            ratingRepository.save(newRating);
            return "Rating added successfully";
        }
    }
}
