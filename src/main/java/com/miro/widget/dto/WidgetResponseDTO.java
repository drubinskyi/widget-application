package com.miro.widget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class WidgetResponseDTO {
    private UUID id;
    private Integer centerX;
    private Integer centerY;
    private Integer zIndex;
    private Integer height;
    private Integer width;
}
