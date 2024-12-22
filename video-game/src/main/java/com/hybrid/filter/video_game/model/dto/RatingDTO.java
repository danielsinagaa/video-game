package com.hybrid.filter.video_game.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {
    private Integer userId;
    private Integer gameId;
    private Integer ratingValue;
}
