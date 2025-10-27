package com.hybrid.filter.video_game.service;

import com.hybrid.filter.video_game.model.entity.GuestPreference;
import com.hybrid.filter.video_game.repository.GuestPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GuestPreferenceService {

    @Autowired
    private GuestPreferenceRepository guestPreferenceRepository;

    public boolean existsByUserId(Integer userId) {
        if (userId == null || userId == 0) return false;
        return guestPreferenceRepository.existsByUserId(userId);
    }

    public void saveGuestPreference(Integer userId, String sessionId, String genreIds) {
        GuestPreference preference = new GuestPreference();
        preference.setUserId(userId);
        preference.setSessionId(sessionId);
        preference.setGenres(genreIds);
        preference.setCreatedAt(LocalDateTime.now());

        guestPreferenceRepository.save(preference);
    }

    public Optional<GuestPreference> findByUserId(Integer userId) {
        if (userId == null || userId == 0) return Optional.empty();
        return guestPreferenceRepository.findByUserId(Long.valueOf(userId));
    }
}
