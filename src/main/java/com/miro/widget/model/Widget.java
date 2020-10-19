package com.miro.widget.model;

import com.miro.widget.dto.WidgetRequestDTO;
import com.miro.widget.dto.WidgetResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
public class Widget {
    private UUID id;
    private Integer centerX;
    private Integer centerY;
    private Integer zIndex;
    private Integer height;
    private Integer width;
    private LocalDateTime lastModified;

    public static Widget fromRequestDTO(WidgetRequestDTO widgetRequestDTO, Integer zIndex) {
        return new Widget(
                UUID.randomUUID(),
                widgetRequestDTO.getCenterX(),
                widgetRequestDTO.getCenterY(),
                zIndex,
                widgetRequestDTO.getHeight(),
                widgetRequestDTO.getWidth(),
                LocalDateTime.now()
        );
    }

    public WidgetResponseDTO toResponseDTO() {
        return new WidgetResponseDTO(
                id,
                centerX,
                centerY,
                zIndex,
                height,
                width
        );
    }

    public Widget updateZIndex(Integer zIndex) {
        return Widget.builder()
                .id(this.id)
                .centerX(this.centerX)
                .centerY(this.centerY)
                .zIndex(zIndex)
                .height(this.height)
                .width(this.width)
                .lastModified(LocalDateTime.now())
                .build();
    }

    public Widget updateWidget(WidgetRequestDTO widgetRequestDTO, Integer zIndex) {
        return Widget.builder()
                .id(this.id)
                .centerX(widgetRequestDTO.getCenterX())
                .centerY(widgetRequestDTO.getCenterY())
                .zIndex(zIndex)
                .height(widgetRequestDTO.getHeight())
                .width(widgetRequestDTO.getWidth())
                .lastModified(LocalDateTime.now())
                .build();
    }

}
