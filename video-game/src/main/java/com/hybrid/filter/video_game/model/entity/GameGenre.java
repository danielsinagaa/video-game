package com.hybrid.filter.video_game.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_genre")
public class GameGenre {
    @EmbeddedId
    private GameGenreId id;

    @ManyToOne
    @MapsId("gameId")
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class GameGenreId implements Serializable {
        private Integer gameId;
        private Integer genreId;
    }
}