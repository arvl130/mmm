package com.ageulin.mmm.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
// Required by @Builder when using the @AllArgsConstructor.
@AllArgsConstructor
// Required by Hibernate.
@NoArgsConstructor
@Entity
@Table(name = "memes")
public class Meme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "img_url")
    private String imgUrl;

    // Exclude relations when serializing to avoid infinite recursion.
    //
    // For more info, see: https://stackoverflow.com/a/56268015
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
