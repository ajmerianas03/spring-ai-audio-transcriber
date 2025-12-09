package com.ai.audio.transcriber.repository;

import com.ai.audio.transcriber.model.TranscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TranscriptionRepository extends JpaRepository<TranscriptionRecord, Long> {
    /**
     * Finds all transcription records created by a specific user ID.
     * This is crucial for multi-user security and data separation.
     * @param userId The ID of the owning user.
     * @return A list of records belonging to the user.
     */
    List<TranscriptionRecord> findByUserIdOrderByCreatedDateDesc(Long userId);

    /**
     * Finds all records for a user that were created after a specific time,
     * allowing us to implement a sliding window log.
     * We only need the date for the rate limit check.
     */
    List<TranscriptionRecord> findByUserIdAndCreatedDateAfter(Long userId, LocalDateTime createdDate);

    /**
     * New method to count records for a specific user created after a given date/time.
     * The Spring Data JPA naming convention automatically generates the query.
     */
    long countByUserIdAndCreatedDateAfter(Long userId, LocalDateTime createdDate);
}