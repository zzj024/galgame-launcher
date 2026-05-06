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
public class Game {
    private Long id;
    private String name;
    private String coverPath;
    private String exePath;
    private String folderPath;
    private Long saveLocationId;
    private String status;          // playing / cleared / dropped / pending
    private Long typeId;
    private String customTags;      // JSON array
    private Boolean isPinned;
    private Boolean isDeleted;
    private Integer playTimeSeconds;
    private String note;
    private LocalDateTime createdAt;
}
