package com.miro.widget.controller;

import com.miro.widget.dto.PagedWidgetResponseDTO;
import com.miro.widget.dto.WidgetRequestDTO;
import com.miro.widget.dto.WidgetResponseDTO;
import com.miro.widget.repository.WidgetRepository;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.miro.widget.util.PageUtil.getPagedResponse;


@RestController
@RequestMapping("/api/widgets")
public class WidgetController {

    private WidgetRepository widgetRepository;

    public WidgetController(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    @PostMapping
    public WidgetResponseDTO createWidget(@Valid @RequestBody WidgetRequestDTO widgetRequestDTO) {
        return widgetRepository.addWidget(widgetRequestDTO).toResponseDTO();
    }

    @PutMapping("/{id}")
    public WidgetResponseDTO updateWidget(@PathVariable String id, @Valid @RequestBody WidgetRequestDTO widgetRequestDTO) {
        return widgetRepository.updateWidget(id, widgetRequestDTO).toResponseDTO();
    }

    @GetMapping("/{id}")
    public WidgetResponseDTO getWidget(@PathVariable String id) {
        return widgetRepository.getWidget(id).toResponseDTO();
    }

    @GetMapping
    public PagedWidgetResponseDTO getAllWidgets(@RequestParam(value = "page") int page,
                                             @RequestParam(value = "limit", required = false, defaultValue = "10") int size) {
        return getPagedResponse(page, size, widgetRepository.getAllWidgets());
    }

    @DeleteMapping("/{id}")
    public void deleteWidget(@PathVariable String id) {
        widgetRepository.deleteWidget(id);
    }
}

