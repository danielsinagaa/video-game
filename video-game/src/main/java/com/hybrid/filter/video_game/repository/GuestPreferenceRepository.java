package com.hybrid.filter.video_game.repository;

import com.hybrid.filter.video_game.model.entity.GuestPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestPreferenceRepository extends JpaRepository<GuestPreference, Long> {
    boolean existsByUserId(Integer userId);
    Optional<GuestPreference> findByUserId(Long userId);
}
