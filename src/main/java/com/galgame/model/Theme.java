package com.galgame.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theme {
    private Long id;
    private String name;
    private String config;          // JSON string
    private Boolean isBuiltin;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
