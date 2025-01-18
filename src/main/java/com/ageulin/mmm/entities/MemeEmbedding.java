package com.ageulin.mmm.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "meme_embeddings")
public class MemeEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "meme_id", nullable = false)
    private Meme meme;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, columnDefinition = "VECTOR(1024)")
    private float[] embedding;
}
