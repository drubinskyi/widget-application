package com.miro.widget.repository;

import com.codepoetics.protonpack.StreamUtils;
import com.miro.widget.dto.WidgetRequestDTO;
import com.miro.widget.error.WidgetNotFoundException;
import com.miro.widget.model.Widget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WidgetRepositoryTest {
    private Map<UUID, Widget> storage = new HashMap<>();
    private TreeMap<Integer, UUID> index = new TreeMap<>();

    private WidgetRepository repository = new WidgetRepository();

    {
        ReflectionTestUtils.setField(repository, "storage", storage);
        ReflectionTestUtils.setField(repository, "index", index);
    }

    @BeforeEach
    public void initEach() {
        storage.clear();
        index.clear();
    }

    @ParameterizedTest
    @MethodSource("valuesForTestAddWithoutBreaks")
    void testAddWidgetWithoutBreaksInSequence(int newZIndex, int lowestZIndex, int highestZIndex) {
        TreeMap<Integer, UUID> oldIndex =
                new TreeMap<>(IntStream.range(lowestZIndex, highestZIndex).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(lowestZIndex, highestZIndex).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        Widget newWidget = repository.addWidget(generateWidgetDTO(newZIndex));

        Assertions.assertEquals(storage.size(), highestZIndex - lowestZIndex + 1);
        Assertions.assertEquals(index.size(), highestZIndex - lowestZIndex + 1);
        Assertions.assertEquals(index.get(newZIndex), newWidget.getId());
        Assertions.assertEquals(storage.get(newWidget.getId()).getZIndex(), newZIndex);

        if (newZIndex <= highestZIndex && newZIndex >= lowestZIndex) {
            IntStream.range(lowestZIndex, newZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
            IntStream.range(newZIndex, highestZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i + 1))
            );
        } else {
            IntStream.range(lowestZIndex, highestZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
        }
    }

    private static Stream<Arguments> valuesForTestAddWithoutBreaks() {
        return Stream.of(
                Arguments.of(-3, -2, 4),
                Arguments.of(0, -5, 8),
                Arguments.of(0, 2, 8),
                Arguments.of(10, -2, 6),
                Arguments.of(4, -4, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesForTestAddWithBreaks")
    void testAddWidgetWithBreaksInSequence(int newZIndex, int lowestZIndex, int highestZIndex) {
        TreeMap<Integer, UUID> oldIndex =
                new TreeMap<>(IntStream.range(lowestZIndex, highestZIndex).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(lowestZIndex, highestZIndex).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        int deltaSize = 5;
        int delta = new Random().nextInt(10) + 2*deltaSize;

        oldIndex.putAll(IntStream.range(highestZIndex + delta - deltaSize, highestZIndex + delta).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(highestZIndex + delta - deltaSize, highestZIndex + delta).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));


        Widget newWidget = repository.addWidget(generateWidgetDTO(newZIndex));

        Assertions.assertEquals(storage.size(), highestZIndex - lowestZIndex + 1 + deltaSize);
        Assertions.assertEquals(index.size(), highestZIndex - lowestZIndex + 1 + deltaSize);
        Assertions.assertEquals(index.get(newZIndex), newWidget.getId());
        Assertions.assertEquals(storage.get(newWidget.getId()).getZIndex(), newZIndex);

        if (newZIndex <= highestZIndex && newZIndex >= lowestZIndex) {
            IntStream.range(lowestZIndex, newZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
            IntStream.range(newZIndex, highestZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i + 1))
            );
            IntStream.range(highestZIndex + delta - deltaSize, highestZIndex + delta).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
        } else {
            IntStream.range(lowestZIndex, highestZIndex).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
            IntStream.range(highestZIndex + delta - deltaSize, highestZIndex + delta).forEach(i ->
                    Assertions.assertEquals(oldIndex.get(i), index.get(i))
            );
        }
    }

    //For testing purposes ZIndex should not exceed the highestZIndex value by more than 5
    private static Stream<Arguments> valuesForTestAddWithBreaks() {
        return Stream.of(
                Arguments.of(-6, -2, 5),
                Arguments.of(0, -3, 8),
                Arguments.of(0, 1, 8),
                Arguments.of(8, -2, 5),
                Arguments.of(4, -4, 4)
        );
    }

    @Test
    void testUpdateWidgetWithLowerZIndex() {
        int lowestZIndex = -2;
        int highestZIndex = 5;
        TreeMap<Integer, UUID> oldIndex =
                new TreeMap<>(IntStream.range(lowestZIndex, highestZIndex).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(lowestZIndex, highestZIndex).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        int oldZIndex = 2;
        UUID widgetId = index.get(oldZIndex);

        int newZIndex = 0;
        int newCenterX = 2;
        int newCenterY = 2;
        int newHeight = 2;
        int newWidth = 2;
        WidgetRequestDTO newWidgetDTO = new WidgetRequestDTO(newCenterX, newCenterY, newZIndex, newHeight, newWidth);
        Widget result = repository.updateWidget(widgetId.toString(), newWidgetDTO);

        Assertions.assertEquals(storage.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(index.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(result.getCenterX(), newCenterX);
        Assertions.assertEquals(result.getCenterY(), newCenterY);
        Assertions.assertEquals(result.getHeight(), newHeight);
        Assertions.assertEquals(result.getWidth(), newWidth);

        Assertions.assertEquals(index.get(newZIndex), widgetId);
        Assertions.assertEquals(storage.get(widgetId), result);
        Assertions.assertEquals(result.getZIndex(), newZIndex);
        IntStream.range(lowestZIndex, newZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i))
        );
        IntStream.range(newZIndex, oldZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i + 1))
        );
        IntStream.range(oldZIndex + 1, highestZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i))
        );
    }

    @Test
    void testUpdateWidgetWithHigherZIndex() {
        int lowestZIndex = -2;
        int highestZIndex = 5;
        TreeMap<Integer, UUID> oldIndex = new TreeMap<>(IntStream.range(lowestZIndex, highestZIndex).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(lowestZIndex, highestZIndex).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        int oldZIndex = 0;
        UUID widgetId = index.get(oldZIndex);

        int newZIndex = 2;
        int newCenterX = 2;
        int newCenterY = 2;
        int newHeight = 2;
        int newWidth = 2;
        WidgetRequestDTO newWidgetDTO = new WidgetRequestDTO(newCenterX, newCenterY, newZIndex, newHeight, newWidth);
        Widget result = repository.updateWidget(widgetId.toString(), newWidgetDTO);

        Assertions.assertEquals(storage.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(index.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(result.getCenterX(), newCenterX);
        Assertions.assertEquals(result.getCenterY(), newCenterY);
        Assertions.assertEquals(result.getHeight(), newHeight);
        Assertions.assertEquals(result.getWidth(), newWidth);

        Assertions.assertEquals(index.get(newZIndex), widgetId);
        Assertions.assertEquals(storage.get(widgetId), result);
        Assertions.assertEquals(result.getZIndex(), newZIndex);
        IntStream.range(lowestZIndex, oldZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i))
        );
        Assertions.assertNull(index.get(oldZIndex));
        IntStream.range(oldZIndex + 1, newZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i))
        );
        IntStream.range(newZIndex, highestZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i + 1))
        );
    }

    @Test
    void testUpdateWidgetWithoutZIndexChange() {
        int lowestZIndex = -2;
        int highestZIndex = 5;
        TreeMap<Integer, UUID> oldIndex =
                new TreeMap<>(IntStream.range(lowestZIndex, highestZIndex).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(lowestZIndex, highestZIndex).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        int zIndex = 0;
        UUID widgetId = index.get(zIndex);

        int newCenterX = 2;
        int newCenterY = 2;
        int newHeight = 2;
        int newWidth = 2;
        WidgetRequestDTO newWidgetDTO = new WidgetRequestDTO(newCenterX, newCenterY, zIndex, newHeight, newWidth);
        Widget result = repository.updateWidget(widgetId.toString(), newWidgetDTO);

        Assertions.assertEquals(storage.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(index.size(), highestZIndex - lowestZIndex);
        Assertions.assertEquals(result.getCenterX(), newCenterX);
        Assertions.assertEquals(result.getCenterY(), newCenterY);
        Assertions.assertEquals(result.getHeight(), newHeight);
        Assertions.assertEquals(result.getWidth(), newWidth);

        Assertions.assertEquals(index.get(zIndex), widgetId);
        Assertions.assertEquals(storage.get(widgetId), result);
        Assertions.assertEquals(result.getZIndex(), zIndex);
        IntStream.range(lowestZIndex, highestZIndex).forEach(i ->
                Assertions.assertEquals(oldIndex.get(i), index.get(i))
        );
    }


    @Test
    void testAddWidgetWithNullZIndex() {
        TreeMap<Integer, UUID> oldIndex = new TreeMap<>(IntStream.range(-2, 4).boxed().collect(Collectors.toMap(Function.identity(), i -> UUID.randomUUID())));
        IntStream.range(-2, 4).forEach(i -> addToStorageAndIndex(oldIndex.get(i), i));

        Widget newWidget = repository.addWidget(generateWidgetDTO(null));
        int newZIndex = newWidget.getZIndex();

        Assertions.assertEquals(storage.size(), 7);
        Assertions.assertEquals(index.size(), 7);
        Assertions.assertEquals(newZIndex, 4);
        Assertions.assertEquals(storage.get(newWidget.getId()).getZIndex(), newZIndex);
    }

    @Test
    void testAddWidgetWithNullZIndexWhenStorageIsEmpty() {
        Widget newWidget = repository.addWidget(generateWidgetDTO(null));
        int newZIndex = newWidget.getZIndex();

        Assertions.assertEquals(storage.size(), 1);
        Assertions.assertEquals(index.size(), 1);
        Assertions.assertEquals(newZIndex, 0);
        Assertions.assertEquals(storage.get(newWidget.getId()).getZIndex(), newZIndex);
    }


    @Test
    void testGetAll() {
        IntStream.range(0, 3).forEach(i -> addToStorageAndIndex(UUID.randomUUID(), i));

        Collection<Widget> allWidgets = repository.getAllWidgets();
        Assertions.assertEquals(allWidgets.size(), 3);
        StreamUtils
                .zipWithIndex(allWidgets.stream())
                .forEach(i ->
                        Assertions.assertEquals(Long.valueOf(i.getIndex()).intValue(), i.getValue().getZIndex())
                );
    }

    @Test
    void testGetById() {
        UUID id = UUID.randomUUID();
        int zIndex = 2;
        addToStorageAndIndex(id, zIndex);

        Widget widget = repository.getWidget(id.toString());

        Assertions.assertEquals(repository.getAllWidgets().size(), 1);
        Assertions.assertEquals(widget.getId(), id);
        Assertions.assertEquals(widget.getZIndex(), zIndex);
        Assertions.assertEquals(storage.get(widget.getId()).getZIndex(), widget.getZIndex());
    }

    @Test
    void testGetByIdShouldReturnNotFoundException() {
        Assertions.assertThrows(WidgetNotFoundException.class, () -> {
            repository.getWidget(UUID.randomUUID().toString());
        });
    }

    @Test
    void testGetByIdShouldReturnIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            repository.getWidget("not valid uud");
        });
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        int zIndex = 2;
        addToStorageAndIndex(id, zIndex);

        repository.deleteWidget(id.toString());

        Assertions.assertEquals(repository.getAllWidgets().size(), 0);
    }

    @Test
    void testDeleteByIdShouldReturnNotFoundException() {
        Assertions.assertThrows(WidgetNotFoundException.class, () -> {
            repository.deleteWidget(UUID.randomUUID().toString());
        });
    }

    @Test
    void testDeleteByIdShouldReturnIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteWidget("not valid uud");
        });
    }

    private void addToStorageAndIndex(UUID id, Integer zIndex) {
        storage.put(id, generateWidget(id, zIndex));
        index.put(zIndex, id);
    }

    private Widget generateWidget(UUID id, Integer zIndex) {
        return new Widget(id, 1, 1, zIndex, 1, 1, LocalDateTime.now());
    }

    private WidgetRequestDTO generateWidgetDTO(Integer zIndex) {
        return new WidgetRequestDTO(1, 1, zIndex, 1, 1);
    }
}