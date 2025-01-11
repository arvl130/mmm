package com.ageulin.mmm.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
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
    private UUID id;

    // Exclude relations when serializing to avoid infinite recursion.
    //
    // For more info, see: https://stackoverflow.com/a/56268015
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
        name = "meme_keywords",
        joinColumns = @JoinColumn(name = "meme_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id", referencedColumnName = "id")
    )
    private Set<Keyword> keywords = new HashSet<>();
}
