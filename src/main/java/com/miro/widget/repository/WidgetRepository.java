package com.miro.widget.repository;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import com.miro.widget.dto.WidgetRequestDTO;
import com.miro.widget.error.WidgetNotFoundException;
import com.miro.widget.model.Widget;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import static com.miro.widget.model.Widget.fromRequestDTO;

@Repository
public class WidgetRepository {
    private Map<UUID, Widget> storage = new HashMap<>();
    private TreeMap<Integer, UUID> index = new TreeMap<>();
    private StampedLock lock = new StampedLock();

    public Widget addWidget(WidgetRequestDTO widgetRequestDTO) {
        long stamp = lock.writeLock();
        try {
            Integer zIndex = widgetRequestDTO.getZIndex();
            if (zIndex == null) {
                if (index.size() > 0) {
                    zIndex = index.lastKey() + 1;
                } else {
                    zIndex = 0;
                }
            }

            Widget widget = fromRequestDTO(widgetRequestDTO, zIndex);
            updateStorageAndIndex(widget.getId(), zIndex, widget);

            return storage.get(widget.getId());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public Widget updateWidget(String widgetId, WidgetRequestDTO widgetRequestDTO) {
        long stamp = lock.writeLock();
        try {
            UUID id = UUID.fromString(widgetId);
            Widget oldWidget = storage.get(id);
            if (oldWidget == null) {
                throw new WidgetNotFoundException(widgetId);
            }
            Integer oldZIndex = oldWidget.getZIndex();
            Integer newZIndex = widgetRequestDTO.getZIndex();

            Widget newWidget = oldWidget.updateWidget(widgetRequestDTO, newZIndex);
            if (!oldZIndex.equals(newZIndex)) {
                if (widgetRequestDTO.getZIndex() == null) {
                    newZIndex = index.lastKey() + 1;
                }
                index.remove(oldZIndex);
                updateStorageAndIndex(id, newZIndex, newWidget);
            } else {
                storage.put(id, newWidget);
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
            Integer zIndex = widget.getZIndex();
            storage.remove(id);
            index.remove(zIndex);
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

    private void updateStorageAndIndex(UUID id, Integer zIndex, Widget widget) {
        storage.put(id, widget);
        if (index.get(zIndex) == null) {
            index.put(zIndex, id);
        } else {
            updateIndexesWithShift(zIndex, id);
            updateStorageWithNewZIndexes(zIndex);
        }
    }

    private void updateIndexesWithShift(Integer zIndex, UUID id) {
        List<Integer> updatedIndexes = getUpdatedIndexes(zIndex);

        int start = updatedIndexes.get(0);
        UUID newValue = id;
        for (int idx = start; idx < start + updatedIndexes.size(); idx++) {
            UUID tmp = index.get(idx);
            index.put(idx, newValue);
            newValue = tmp;
        }
    }

    private void updateStorageWithNewZIndexes(Integer zIndex) {
        List<Integer> updatedIndexes = getUpdatedIndexes(zIndex);
        Map<Integer, UUID> map;

        Map.Entry<Integer, UUID> headEntry = index.higherEntry(updatedIndexes.get(updatedIndexes.size() - 1));
        if (headEntry != null) {
            map = index.tailMap(zIndex + 1).headMap(headEntry.getKey());
        } else {
            map = index.tailMap(zIndex + 1);
        }

        map.forEach((key, value) -> {
            Widget oldWidget = storage.get(value);
            storage.put(value, oldWidget.updateZIndex(key));
        });
    }

    //Retrieve list of all incremented indexes
    private List<Integer> getUpdatedIndexes(Integer zIndex) {
        List<Integer> indexesToUpdate = StreamUtils.zipWithIndex(index.tailMap(zIndex).keySet().stream())
                .takeWhile(i -> i.getIndex() + zIndex == i.getValue())
                .map(Indexed::getValue)
                .collect(Collectors.toList());
        indexesToUpdate.add(indexesToUpdate.get(indexesToUpdate.size() - 1) + 1);

        return indexesToUpdate;
    }

}
