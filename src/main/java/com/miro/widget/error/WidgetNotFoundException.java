package com.miro.widget.error;

public class WidgetNotFoundException extends RuntimeException {
    public WidgetNotFoundException(String id) {
        super(String.format("Widget with id=%s not found", id));
    }
}
