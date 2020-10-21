package com.miro.widget.model;

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

    public Widget updateZIndex(Integer zIndex) {
        return Widget.builder()
                .id(this.id)
                .centerX(this.centerX)
                .centerY(this.centerY)
                .zIndex(zIndex)
                .height(this.height)
                .width(this.width)
                .lastModified(this.lastModified)
                .build();
    }

    public Widget updateId(UUID id) {
        return Widget.builder()
                .id(id)
                .centerX(this.centerX)
                .centerY(this.centerY)
                .zIndex(this.zIndex)
                .height(this.height)
                .width(this.width)
                .lastModified(this.lastModified)
                .build();
    }
}
