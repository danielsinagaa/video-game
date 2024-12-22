package com.hybrid.filter.video_game.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingForm {
    private Long userId;
    private Long gameId;
    private int ratingValue;
}
