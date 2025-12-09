package com.ai.audio.transcriber.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transcription_records")
public class TranscriptionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to the User who created this record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String originalFileName;
    private LocalDateTime createdDate = LocalDateTime.now();

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fullTranscription;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String aiAnalysis;
}