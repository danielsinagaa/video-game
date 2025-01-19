package com.hybrid.filter.video_game.model.dto;

import com.hybrid.filter.video_game.model.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopGenreFilterDTO {
    private Integer id;
    private String name;
    private List<GameFilterDTO> gameFilters;
}
