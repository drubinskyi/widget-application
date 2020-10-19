package com.miro.widget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public class PagedWidgetResponseDTO {
    private Collection<WidgetResponseDTO> result;
    private Integer page;
    private Integer totalPages;
}
