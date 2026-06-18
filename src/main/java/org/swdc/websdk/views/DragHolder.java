package org.swdc.websdk.views;

import jakarta.inject.Singleton;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class DragHolder {

    private Map<Class, Object> dragging = new HashMap<>();

    private Map<Class, Object> dropped = new HashMap<>();

    public void startDrag(Class objectType, Object draggable, Dragboard dragboard) {

        dragging.put(objectType, draggable);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(objectType.getName());
        dragboard.setContent(clipboardContent);

    }

    public boolean isDragging(Class objectType) {
        return dragging.containsKey(objectType);
    }

    public <T> T finishDrag(Class<T> objectType) {
        if (dragging.containsKey(objectType)) {
            Object draggable = dragging.remove(objectType);
            dropped.put(objectType, draggable);
            return (T) draggable;
        }
        return null;
    }

    public <T> T getDropped(Class<T> objectType) {
        return (T) dropped.remove(objectType);
    }

}
