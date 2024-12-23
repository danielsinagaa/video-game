package com.hybrid.filter.video_game.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private int id;
    private String title;
    private String description;
    private String image;
    private Date releaseDate;
    private String dateString;
    private String developer;
    private Double rating;
    private int ratingSum;
    private String genres;
    private String steamLink;
    private Double price;
}
