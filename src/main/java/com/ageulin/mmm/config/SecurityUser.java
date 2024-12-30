package com.ageulin.mmm.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SecurityUser implements Serializable {
    private final UUID id;
}
