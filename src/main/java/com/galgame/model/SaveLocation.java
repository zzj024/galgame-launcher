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
public class SaveLocation {
    private Long id;
    private String path;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
