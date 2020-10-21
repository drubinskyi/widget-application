package com.miro.widget.util;

import com.miro.widget.dto.PagedWidgetResponseDTO;
import com.miro.widget.dto.WidgetResponseDTO;
import com.miro.widget.error.BadRequestException;
import com.miro.widget.model.Widget;

import java.util.Collection;
import java.util.stream.Collectors;

public class PageUtil {

    public static PagedWidgetResponseDTO getPagedResponse(int page, int limit, Collection<Widget> widgets) {
        if (limit <= 0) {
            throw new BadRequestException("Limit should be positive");
        }

        if (limit > 500) {
            throw new BadRequestException("Limit can't be higher than 500");
        }

        if (page < 1) {
            throw new BadRequestException("Page number can't be less then 1");
        }

        int totalSize = widgets.size();
        int totalPages = totalSize % limit == 0 ? totalSize / limit : totalSize / limit + 1;

        return new PagedWidgetResponseDTO(
                widgets.stream().skip(limit * (page - 1)).limit(limit).map(WidgetResponseDTO::fromWidget).collect(Collectors.toList()),
                page,
                totalPages
        );

    }

}
