package com.miro.widget.dto;

import com.miro.widget.model.Widget;
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

    public static WidgetResponseDTO fromWidget(Widget widget) {
        return new WidgetResponseDTO(
                widget.getId(),
                widget.getCenterX(),
                widget.getCenterY(),
                widget.getZIndex(),
                widget.getHeight(),
                widget.getWidth()
        );
    }

}
