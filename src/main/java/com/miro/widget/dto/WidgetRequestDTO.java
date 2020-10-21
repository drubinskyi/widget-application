package com.miro.widget.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miro.widget.model.Widget;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class WidgetRequestDTO {
    @NotNull(message = "centerX should not be null")
    private Integer centerX;
    @NotNull(message = "centerY should not be null")
    private Integer centerY;
    private Integer zIndex;
    @Positive(message = "Height should be > 0")
    private Integer height;
    @Positive(message = "Width should be > 0")
    private Integer width;

    @JsonCreator
    public WidgetRequestDTO(@JsonProperty("centerX") Integer centerX,
                            @JsonProperty("centerY") Integer centerY,
                            @JsonProperty("zIndex") Integer zIndex,
                            @JsonProperty("height") Integer height,
                            @JsonProperty("width") Integer width
    ) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zIndex = zIndex;
        this.height = height;
        this.width = width;
    }

    public Widget toWidget() {
        return new Widget(
                UUID.randomUUID(),
                centerX,
                centerY,
                zIndex,
                height,
                width,
                LocalDateTime.now()
        );
    }
}
