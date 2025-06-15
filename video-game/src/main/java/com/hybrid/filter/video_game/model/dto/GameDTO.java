package com.hybrid.filter.video_game.model.dto;

import com.hybrid.filter.video_game.model.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

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
    private Double priceDouble;
    private String price;
    private Double priceView;
    private List<Genre> genreList;
    
    public void convertRupiah(Double price){
        this.price = new DecimalFormat("#,###").format(price);
    }
}

