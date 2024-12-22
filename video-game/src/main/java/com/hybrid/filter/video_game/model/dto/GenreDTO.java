package com.hybrid.filter.video_game.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO {

    private Integer id;

    private String name;

    private byte[] genreImage;

    private String genreImage64;
}
