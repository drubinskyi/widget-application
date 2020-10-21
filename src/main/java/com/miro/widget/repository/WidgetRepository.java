package com.miro.widget.repository;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import com.miro.widget.error.WidgetNotFoundException;
import com.miro.widget.model.Widget;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

@Repository
public class WidgetRepository {
    private Map<UUID, Widget> storage = new HashMap<>();
    private TreeMap<Integer, UUID> index = new TreeMap<>();
    private StampedLock lock = new StampedLock();

    public Widget addWidget(Widget newWidget) {
        long stamp = lock.writeLock();
        try {
            Integer zIndex = newWidget.getZIndex();
            if (zIndex == null) {
                if (index.size() > 0) {
                    zIndex = index.lastKey() + 1;
                } else {
                    zIndex = 0;
                }
            }

            Widget widget = newWidget.updateZIndex(zIndex);
            updateStorageAndIndex(widget);

            return storage.get(widget.getId());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public Widget updateWidget(String widgetId, Widget newWidget) {
        long stamp = lock.writeLock();
        try {
            UUID id = UUID.fromString(widgetId);
            Widget oldWidget = storage.get(id);
            if (oldWidget == null) {
                throw new WidgetNotFoundException(widgetId);
            }

            Integer oldZIndex = oldWidget.getZIndex();
            Integer newZIndex = newWidget.getZIndex();
            if (newZIndex == null) {
                newZIndex = index.lastKey() + 1;
                newWidget.updateZIndex(newZIndex);
            }

            Widget widget = newWidget.updateId(id);

            if (!oldZIndex.equals(newZIndex)) {
                index.remove(oldZIndex);
                updateStorageAndIndex(widget);
            } else {
                storage.put(id, widget);
            }

            return storage.get(id);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void deleteWidget(String widgetId) {
        long stamp = lock.writeLock();
        try {
            UUID id = UUID.fromString(widgetId);
            Widget widget = storage.get(id);
            if (widget == null) {
                throw new WidgetNotFoundException(widgetId);
            }

            storage.remove(id);
            index.remove(widget.getZIndex());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public Widget getWidget(String widgetId) {
        long stamp = lock.tryOptimisticRead();
        UUID id = UUID.fromString(widgetId);
        Widget result = storage.get(id);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = storage.get(id);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if (result == null) {
            throw new WidgetNotFoundException(widgetId);
        }

        return result;
    }

    public Collection<Widget> getAllWidgets() {
        long stamp = lock.tryOptimisticRead();
        Collection<Widget> result = index.values().stream().map(storage::get).collect(Collectors.toList());

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = index.entrySet().stream().map(storage::get).collect(Collectors.toList());
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return result;
    }

    private void updateStorageAndIndex(Widget widget) {
        storage.put(widget.getId(), widget);
        if (index.get(widget.getZIndex()) == null) {
            index.put(widget.getZIndex(), widget.getId());
        } else {
            updateStorageAndIndexWithShift(widget);
        }
    }

    private void updateStorageAndIndexWithShift(Widget widget) {
        int zIndex = widget.getZIndex();

        // Slicing all indexes which have to be shifted
        List<Map.Entry<Integer, UUID>> entryList = StreamUtils.zipWithIndex(index.tailMap(widget.getZIndex()).entrySet().stream())
                .takeWhile(entryIndexed -> entryIndexed.getIndex() + zIndex == entryIndexed.getValue().getKey())
                .map(Indexed::getValue)
                .collect(Collectors.toList());

        int headShiftedValue = entryList.get(entryList.size() - 1).getKey() + 1;

        // Updating storage and index with new values
        UUID newValue = widget.getId();
        for (Map.Entry<Integer, UUID> entry : entryList) {
            UUID tmp = entry.getValue();
            entry.setValue(newValue);
            newValue = tmp;

            Widget oldWidget = storage.get(entry.getValue());
            storage.put(entry.getValue(), oldWidget.updateZIndex(entry.getKey()));
        }
        // Adding the value to the index and storage, which was obtained as a result of the shift
        index.put(headShiftedValue, newValue);
        Widget headWidget = storage.get(newValue);
        storage.put(newValue, headWidget.updateZIndex(headShiftedValue));
    }
}
