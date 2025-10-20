package com.hybrid.filter.video_game.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_preference_genre")
public class GuestPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Jika user login, simpan userId, jika tidak maka null (guest)
    @Column(name = "user_id")
    private Integer userId;

    // Session ID tetap digunakan untuk guest
    private String sessionId;

    // Simpan list genre dalam bentuk string, misal "1,3,5"
    private String genres;

    private LocalDateTime createdAt = LocalDateTime.now();
}
